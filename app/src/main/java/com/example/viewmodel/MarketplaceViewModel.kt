package com.example.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MarketplaceViewModel(private val repository: MarketplaceRepository) : ViewModel() {

    private val _currentUserId = MutableStateFlow("current_user_id")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // --- Core Database Flows ---
    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id -> repository.getUserFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val topSellers: StateFlow<List<UserEntity>> = repository.getTopSellers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProducts: StateFlow<List<ProductEntity>> = repository.getFeaturedProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingProducts: StateFlow<List<ProductEntity>> = repository.getTrendingProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKycRequests: StateFlow<List<VerificationRequestEntity>> = repository.getAllVerificationRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search & Filters State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _filterVerifiedOnly = MutableStateFlow(false)
    val filterVerifiedOnly: StateFlow<Boolean> = _filterVerifiedOnly.asStateFlow()

    private val _maxPriceFilter = MutableStateFlow<Float?>(null)
    val maxPriceFilter: StateFlow<Float?> = _maxPriceFilter.asStateFlow()

    private val _sortOption = MutableStateFlow("Latest") // Latest, Price Low-High, Price High-Low, Downloads, Rating
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()

    private data class ProductFilters(
        val query: String,
        val category: String,
        val verifiedOnly: Boolean,
        val maxPrice: Float?,
        val sort: String
    )

    private val filterStateFlow: Flow<ProductFilters> = combine(
        searchQuery, selectedCategory, filterVerifiedOnly, maxPriceFilter, sortOption
    ) { query, category, verifiedOnly, maxPrice, sort ->
        ProductFilters(query, category, verifiedOnly, maxPrice, sort)
    }

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts, filterStateFlow, topSellers
    ) { products, filters, sellers ->
        val query = filters.query
        val category = filters.category
        val verifiedOnly = filters.verifiedOnly
        val maxPrice = filters.maxPrice
        val sort = filters.sort

        var list = products.filter { product ->
            val matchesQuery = query.isBlank() || product.title.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.tags.contains(query, ignoreCase = true)
            
            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)
            
            val matchesVerified = !verifiedOnly || (sellers.find { it.userId == product.sellerId }?.verifiedBadge ?: false)
            
            val matchesPrice = maxPrice == null || (product.price <= maxPrice)

            matchesQuery && matchesCategory && matchesVerified && matchesPrice
        }

        list = when (sort) {
            "Price Low-High" -> list.sortedBy { if (it.discountedPrice > 0.0) it.discountedPrice else it.price }
            "Price High-Low" -> list.sortedByDescending { if (it.discountedPrice > 0.0) it.discountedPrice else it.price }
            "Downloads" -> list.sortedByDescending { it.downloadsCount }
            "Rating" -> list.sortedByDescending { it.rating }
            else -> list.sortedByDescending { it.createdTimestamp } // Latest
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Cart State ---
    val cartItems = mutableStateListOf<ProductEntity>()
    private val _appliedCoupon = MutableStateFlow("")
    val appliedCoupon: StateFlow<String> = _appliedCoupon.asStateFlow()

    val couponDiscountPercent = _appliedCoupon.map { coupon ->
        if (coupon.uppercase() == "PRIME20") 20 else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal = derivedStateOf {
        cartItems.sumOf { if (it.discountedPrice > 0.0) it.discountedPrice else it.price }
    }

    val cartTotal = derivedStateOf {
        val sub = cartSubtotal.value
        val discount = (sub * (couponDiscountPercent.value / 100.0))
        sub - discount
    }

    // --- Active Chat System State ---
    private val _activeChatPartner = MutableStateFlow<UserEntity?>(null)
    val activeChatPartner: StateFlow<UserEntity?> = _activeChatPartner.asStateFlow()

    val chatMessages: StateFlow<List<MessageEntity>> = combine(
        _currentUserId, _activeChatPartner
    ) { currId, partner ->
        if (partner == null) emptyList()
        else {
            val chatId = getChatId(currId, partner.userId)
            repository.getMessagesForChat(chatId).first()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Current Notifications State ---
    val notificationsFlow: StateFlow<List<NotificationEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getNotificationsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Current Wallet Transaction Flow ---
    val walletTransactionsFlow: StateFlow<List<TransactionEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getTransactionsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Current User Orders Library Flow ---
    val userOrdersFlow: StateFlow<List<OrderEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getOrdersByBuyer(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- KYC Verification Request Flow ---
    val myKycRequest: StateFlow<VerificationRequestEntity?> = _currentUserId
        .flatMapLatest { id ->
            flow {
                emit(repository.getRequestByUserId(id))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Product detail reviews ---
    private val _activeProductIdForReviews = MutableStateFlow<Int?>(null)
    val activeProductReviews: StateFlow<List<ReviewEntity>> = _activeProductIdForReviews
        .flatMapLatest { pId ->
            if (pId == null) flowOf(emptyList())
            else repository.getReviewsForProduct(pId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        // Ensure database seed data is preloaded upon viewModel instantiation
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    private fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    // --- Actions ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun toggleVerifiedOnly() {
        _filterVerifiedOnly.value = !_filterVerifiedOnly.value
    }

    fun setPriceFilter(max: Float?) {
        _maxPriceFilter.value = max
    }

    fun setSortOption(option: String) {
        _sortOption.value = option
    }

    // --- Cart Actions ---
    fun addToCart(product: ProductEntity) {
        if (!cartItems.contains(product)) {
            cartItems.add(product)
        }
    }

    fun removeFromCart(product: ProductEntity) {
        cartItems.remove(product)
    }

    fun applyCoupon(code: String) {
        _appliedCoupon.value = code
    }

    fun clearCart() {
        cartItems.clear()
        _appliedCoupon.value = ""
    }

    fun checkoutCart(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch onError("Please log in first")
            val total = cartTotal.value

            if (user.walletBalance < total) {
                onError("Insufficient Funds inside your Prime Wallet! Please deposit more funds.")
                return@launch
            }

            // Deduct from wallet
            val updatedUser = user.copy(walletBalance = user.walletBalance - total)
            repository.updateUser(updatedUser)

            // Log Transaction
            val transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
            repository.insertTransaction(
                TransactionEntity(
                    transactionId = transactionId,
                    userId = user.userId,
                    amount = total,
                    type = "PURCHASE",
                    description = "Purchase of ${cartItems.size} digital goods from Cart",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Deliver digital files as orders
            for (p in cartItems) {
                val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
                val license = "PRIME-${UUID.randomUUID().toString().take(12).uppercase()}"
                
                // Add Order
                repository.insertOrder(
                    OrderEntity(
                        orderId = orderId,
                        productId = p.productId,
                        productName = p.title,
                        buyerId = user.userId,
                        sellerId = p.sellerId,
                        amountPaid = if (p.discountedPrice > 0.0) p.discountedPrice else p.price,
                        licenseKey = license,
                        secureDownloadLink = p.downloadUrl.ifBlank { "https://prime-secure.downloads.net/file/${p.productId}/dl_token=temp_tkn_84fa92" },
                        downloadsUsed = 0,
                        maxDownloads = 5,
                        isDelivered = true,
                        timestamp = System.currentTimeMillis()
                    )
                )

                // Notify seller of a sale and add balance
                val seller = repository.getUser(p.sellerId)
                if (seller != null) {
                    val pSales = if (p.discountedPrice > 0.0) p.discountedPrice else p.price
                    val updatedSeller = seller.copy(
                        walletBalance = seller.walletBalance + pSales,
                        totalSales = seller.totalSales + 1
                    )
                    repository.updateUser(updatedSeller)

                    repository.insertTransaction(
                        TransactionEntity(
                            transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                            userId = seller.userId,
                            amount = pSales,
                            type = "SALE",
                            description = "Sold '${p.title}' digital item",
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    repository.insertNotification(
                        NotificationEntity(
                            userId = seller.userId,
                            title = "item Sold! +$${String.format("%.2f", pSales)}",
                            text = "A buyer purchased your product '${p.title}'! Your earnings have been credited to your wallet.",
                            type = "sale"
                        )
                    )
                }
            }

            // Create notification for buyer
            repository.insertNotification(
                NotificationEntity(
                    userId = user.userId,
                    title = "Order Placed Successfully",
                    text = "You purchased ${cartItems.size} item(s). Your digital files, temp secure download links, and license keys are now ready in your Library!",
                    type = "order"
                )
            )

            clearCart()
            onSuccess()
        }
    }

    // --- Instant Checkout ---
    fun instantBuyProduct(product: ProductEntity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch onError("Please log in first")
            val pPrice = if (product.discountedPrice > 0.0) product.discountedPrice else product.price

            if (user.walletBalance < pPrice) {
                onError("Insufficient Funds inside your Prime Wallet! Please deposit more funds.")
                return@launch
            }

            // Deduct
            val updatedUser = user.copy(walletBalance = user.walletBalance - pPrice)
            repository.updateUser(updatedUser)

            // Log Transaction
            val transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
            repository.insertTransaction(
                TransactionEntity(
                    transactionId = transactionId,
                    userId = user.userId,
                    amount = pPrice,
                    type = "PURCHASE",
                    description = "Instant buy of '${product.title}'",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Deliver Order
            val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
            val license = "PRIME-${UUID.randomUUID().toString().take(12).uppercase()}"
            repository.insertOrder(
                OrderEntity(
                    orderId = orderId,
                    productId = product.productId,
                    productName = product.title,
                    buyerId = user.userId,
                    sellerId = product.sellerId,
                    amountPaid = pPrice,
                    licenseKey = license,
                    secureDownloadLink = product.downloadUrl.ifBlank { "https://prime-secure.downloads.net/file/${product.productId}/dl_token=temp_tkn_84fa92" },
                    downloadsUsed = 0,
                    maxDownloads = 5,
                    isDelivered = true,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Notify seller and credit Wallet
            val seller = repository.getUser(product.sellerId)
            if (seller != null) {
                val updatedSeller = seller.copy(
                    walletBalance = seller.walletBalance + pPrice,
                    totalSales = seller.totalSales + 1
                )
                repository.updateUser(updatedSeller)

                repository.insertTransaction(
                    TransactionEntity(
                        transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                        userId = seller.userId,
                        amount = pPrice,
                        type = "SALE",
                        description = "Sold '${product.title}' product",
                        timestamp = System.currentTimeMillis()
                    )
                )

                repository.insertNotification(
                    NotificationEntity(
                        userId = seller.userId,
                        title = "item Sold! +$${String.format("%.2f", pPrice)}",
                        text = "A buyer completed instant-purchase for '${product.title}'!",
                        type = "sale"
                    )
                )
            }

            repository.insertNotification(
                NotificationEntity(
                    userId = user.userId,
                    title = "Instant Purchase Success",
                    text = "You purchased '${product.title}'! Secure files and licensing details have been added to your vault.",
                    type = "order"
                )
            )

            onSuccess()
        }
    }

    // --- Onboarding / Auth Simulation ---
    fun loginOrRegister(email: String, username: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            // Check if user exists
            val existing = repository.getUser(email)
            if (existing != null) {
                _currentUserId.value = existing.userId
            } else {
                // Register a new user
                val newUserId = "user_${UUID.randomUUID().toString().take(8)}"
                val newUser = UserEntity(
                    userId = newUserId,
                    username = username.ifBlank { email.substringBefore("@") },
                    email = email,
                    bio = "Newly verified user on Prime Marketplace. Trading digital products responsibly.",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200",
                    verifiedBadge = false,
                    sellerLevel = "Beginner",
                    totalSales = 0,
                    rating = 0.0f,
                    walletBalance = 250.0, // Welcome gift of $250.0 for testing
                    joinedDate = "May 2026"
                )
                repository.insertUser(newUser)
                _currentUserId.value = newUserId

                repository.insertNotification(
                    NotificationEntity(
                        userId = newUserId,
                        title = "Welcome Bonus Preloaded",
                        text = "Thank you for joining Prime Marketplace! We have preloaded your wallet with a welcome credit of $250.00.",
                        type = "sale"
                    )
                )
            }
            onComplete()
        }
    }

    fun logout() {
        _currentUserId.value = "current_user_id" // revert back to default Alex Creator account
    }

    // --- Seller Following System ---
    fun toggleFollowSeller(sellerId: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val followingList = user.followingIds.split(",").filter { it.isNotBlank() }.toMutableList()
            val sellerUser = repository.getUser(sellerId) ?: return@launch
            var isNowFollowing = false

            if (followingList.contains(sellerId)) {
                followingList.remove(sellerId)
                // decrement followersCount
                repository.updateUser(sellerUser.copy(followersCount = (sellerUser.followersCount - 1).coerceAtLeast(0)))
            } else {
                followingList.add(sellerId)
                isNowFollowing = true
                repository.updateUser(sellerUser.copy(followersCount = sellerUser.followersCount + 1))
                
                // notify seller
                repository.insertNotification(
                    NotificationEntity(
                        userId = sellerId,
                        title = "New Follower alert!",
                        text = "@${user.username} is now following your shop dashboard for core drops.",
                        type = "follower"
                    )
                )
            }

            repository.updateUser(user.copy(followingIds = followingList.joinToString(",")))
        }
    }

    // --- Active Reviews Picker ---
    fun selectProductReviews(productId: Int) {
        _activeProductIdForReviews.value = productId
    }

    // --- Post reviews ---
    fun addReview(productId: Int, rating: Int, comment: String, imageUrl: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val newReview = ReviewEntity(
                productId = productId,
                userId = user.userId,
                userName = user.username,
                userAvatar = user.avatarUrl,
                rating = rating,
                comment = comment,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            repository.insertReview(newReview)

            // Update product average rating
            val product = repository.getProductById(productId)
            if (product != null) {
                val newCount = product.ratingCount + 1
                val newAvg = ((product.rating * product.ratingCount) + rating) / newCount
                repository.insertProduct(product.copy(
                    rating = newAvg,
                    ratingCount = newCount
                ))
            }
        }
    }

    // --- Chat Actions ---
    fun selectActiveChat(partner: UserEntity) {
        _activeChatPartner.value = partner
        viewModelScope.launch {
            val chatId = getChatId(currentUserId.value, partner.userId)
            repository.markChatAsSeen(chatId, currentUserId.value)
        }
    }

    fun closeChat() {
        _activeChatPartner.value = null
    }

    fun sendMessage(text: String, imageUrl: String = "", attachmentUrl: String = "") {
        val partner = _activeChatPartner.value ?: return
        val senderId = currentUserId.value
        val chatId = getChatId(senderId, partner.userId)

        viewModelScope.launch {
            val message = MessageEntity(
                chatId = chatId,
                senderId = senderId,
                receiverId = partner.userId,
                messageText = text,
                imageUrl = imageUrl,
                attachmentUrl = attachmentUrl,
                timestamp = System.currentTimeMillis(),
                isSeen = false
            )
            repository.insertMessage(message)

            // Trigger mock auto replies if chatting with seed sellers
            simulateMockSellerReply(partner, text)
        }
    }

    private suspend fun simulateMockSellerReply(seller: UserEntity, userMsg: String) {
        // Trigger simulated reply after 1.5 seconds if they chat with a seeded developer account
        if (seller.userId.startsWith("seller_")) {
            kotlinx.coroutines.delay(1500)
            val replyMsg = when {
                userMsg.contains("price", ignoreCase = true) || userMsg.contains("discount", ignoreCase = true) -> {
                    "Hello! All prices are fixed in Prime tokens, but you can try code PRIME20 inside the cart to enjoy a nice 20% discount on your checkout pool."
                }
                userMsg.contains("source", ignoreCase = true) || userMsg.contains("compile", ignoreCase = true) -> {
                    "Yes, all project templates are written in pristine Kotlin & Jetpack Compose and are fully ready to compile inside latest Android Studio."
                }
                userMsg.contains("buy", ignoreCase = true) || userMsg.contains("download", ignoreCase = true) -> {
                    "Once you check out or click 'Instant Buy', the secure file links and official dynamic license keys instantly activate in your library page!"
                }
                else -> "Greetings! Thank you for reaching out to @${seller.username}. Your message has been logged. Let me know if you would like custom templates, source-codes, or consulting services."
            }
            val chatId = getChatId(currentUserId.value, seller.userId)
            repository.insertMessage(
                MessageEntity(
                    chatId = chatId,
                    senderId = seller.userId,
                    receiverId = currentUserId.value,
                    messageText = replyMsg,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Notify user of new message
            repository.insertNotification(
                NotificationEntity(
                    userId = currentUserId.value,
                    title = "New chat from @${seller.username}",
                    text = replyMsg.take(50) + "...",
                    type = "message"
                )
            )
        }
    }

    // --- Upload Product Actions ---
    fun uploadProduct(
        title: String,
        description: String,
        price: Double,
        discountedPrice: Double,
        category: String,
        imageUrls: String,
        downloadUrl: String,
        tags: String,
        compatibility: String,
        fileSize: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val product = ProductEntity(
                title = title,
                description = description,
                price = price,
                discountedPrice = discountedPrice,
                category = category,
                imageUrls = imageUrls.ifBlank { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&q=80&w=500" },
                downloadUrl = downloadUrl.ifBlank { "https://prime-secure.downloads.net/file/custom/bundle_zip" },
                tags = tags,
                compatibility = compatibility.ifBlank { "Android 8.0+" },
                fileSize = fileSize.ifBlank { "10 MB" },
                sellerId = user.userId,
                isFeatured = false,
                isTrending = false
            )
            repository.insertProduct(product)

            repository.insertNotification(
                NotificationEntity(
                    userId = user.userId,
                    title = "Product Listed Live",
                    text = "Your digital asset '${product.title}' has been processed and is now available to browse publicly under $category category.",
                    type = "sale"
                )
            )

            onSuccess()
        }
    }

    // --- Wallet Actions ---
    fun depositFunds(amount: Double) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updated = user.copy(walletBalance = user.walletBalance + amount)
            repository.updateUser(updated)

            val txId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
            repository.insertTransaction(
                TransactionEntity(
                    transactionId = txId,
                    userId = user.userId,
                    amount = amount,
                    type = "DEPOSIT",
                    description = "Added deposit funds via secure card simulation",
                    timestamp = System.currentTimeMillis()
                )
            )

            repository.insertNotification(
                NotificationEntity(
                    userId = user.userId,
                    title = "Wallet Deposit Success",
                    text = "You deposited +$${String.format("%.2f", amount)} inside your Prime balance wallet pool.",
                    type = "sale"
                )
            )
        }
    }

    fun withdrawFunds(amount: Double, destAccount: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            if (user.walletBalance < amount) {
                onError("Insufficient wallet token balance.")
                return@launch
            }

            val updated = user.copy(walletBalance = user.walletBalance - amount)
            repository.updateUser(updated)

            val txId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
            repository.insertTransaction(
                TransactionEntity(
                    transactionId = txId,
                    userId = user.userId,
                    amount = amount,
                    type = "WITHDRAWAL",
                    description = "Withdrawn funds to $destAccount",
                    timestamp = System.currentTimeMillis()
                )
            )

            repository.insertNotification(
                NotificationEntity(
                    userId = user.userId,
                    title = "Wallet Withdrawal Executed",
                    text = "Initiated secure transfer of -$${String.format("%.2f", amount)} to account $destAccount.",
                    type = "order"
                )
            )

            onSuccess()
        }
    }

    // --- KYC / Verification Actions ---
    fun submitKycVerification(realName: String, idProofLink: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val request = VerificationRequestEntity(
                userId = user.userId,
                realName = realName,
                idProofLink = idProofLink,
                status = "PENDING"
            )
            repository.insertVerificationRequest(request)
            onSuccess()
        }
    }

    // --- Admin Dashboard Actions ---
    fun adminApproveKyc(requestId: String) {
        viewModelScope.launch {
            val req = repository.getRequestByUserId(requestId) ?: return@launch
            val updatedReq = req.copy(status = "APPROVED", notes = "Manually verified by marketplace administrators")
            repository.updateVerificationRequest(updatedReq)

            // Update user to have a blue tick and Elite seller status
            val user = repository.getUser(requestId)
            if (user != null) {
                val updatedUser = user.copy(
                    verifiedBadge = true,
                    sellerLevel = "Verified Elite"
                )
                repository.updateUser(updatedUser)

                repository.insertNotification(
                    NotificationEntity(
                        userId = requestId,
                        title = "KYC Verification Approved! 🌟",
                        text = "Congratulations! You have been granted the verified blue tick badge and Seller Level: 'Verified Elite'.",
                        type = "follower"
                    )
                )
            }
        }
    }

    fun adminRejectKyc(requestId: String, notes: String) {
        viewModelScope.launch {
            val req = repository.getRequestByUserId(requestId) ?: return@launch
            val updatedReq = req.copy(status = "REJECTED", notes = notes)
            repository.updateVerificationRequest(updatedReq)

            repository.insertNotification(
                NotificationEntity(
                    userId = requestId,
                    title = "Verification KYC Status Update",
                    text = "Your request for storefront verified status was not accepted. Reason: $notes",
                    type = "order"
                )
            )
        }
    }

    fun adminToggleFeatured(productId: Int) {
        viewModelScope.launch {
            val prod = repository.getProductById(productId) ?: return@launch
            repository.insertProduct(prod.copy(isFeatured = !prod.isFeatured))
        }
    }

    fun adminToggleTrending(productId: Int) {
        viewModelScope.launch {
            val prod = repository.getProductById(productId) ?: return@launch
            repository.insertProduct(prod.copy(isTrending = !prod.isTrending))
        }
    }

    fun adminBanOrDeleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            
            // notify seller
            repository.insertNotification(
                NotificationEntity(
                    userId = product.sellerId,
                    title = "Asset Revoked / Removed",
                    text = "Your digital listing '${product.title}' was flagged and manually removed by administrator moderation.",
                    type = "order"
                )
            )
        }
    }

    fun markNotificationsRead(userId: String) {
        viewModelScope.launch {
            repository.markNotificationsRead(userId)
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }
}

class MarketplaceViewModelFactory(private val repository: MarketplaceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketplaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketplaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
