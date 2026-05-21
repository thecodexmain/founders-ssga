package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class MarketplaceRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val productDao = db.productDao()
    private val orderDao = db.orderDao()
    private val messageDao = db.messageDao()
    private val notificationDao = db.notificationDao()
    private val verificationRequestDao = db.verificationRequestDao()
    private val transactionDao = db.transactionDao()
    private val reviewDao = db.reviewDao()

    // --- Users ---
    suspend fun getUser(userId: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    fun getUserFlow(userId: String): Flow<UserEntity?> = userDao.getUserFlowById(userId)

    fun getTopSellers(): Flow<List<UserEntity>> = userDao.getTopSellers()

    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()


    // --- Products ---
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getFeaturedProducts(): Flow<List<ProductEntity>> = productDao.getFeaturedProducts()

    fun getTrendingProducts(): Flow<List<ProductEntity>> = productDao.getTrendingProducts()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> = productDao.getProductsByCategory(category)

    suspend fun getProductById(productId: Int): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductById(productId)
    }

    fun getProductFlow(productId: Int): Flow<ProductEntity?> = productDao.getProductFlowById(productId)

    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>> = productDao.getProductsBySeller(sellerId)

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun insertProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }


    // --- Orders ---
    fun getOrdersByBuyer(buyerId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByBuyer(buyerId)

    fun getOrdersBySeller(sellerId: String): Flow<List<OrderEntity>> = orderDao.getOrdersBySeller(sellerId)

    suspend fun getOrderById(orderId: String): OrderEntity? = withContext(Dispatchers.IO) {
        orderDao.getOrderById(orderId)
    }

    suspend fun insertOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
    }

    suspend fun updateOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.updateOrder(order)
    }


    // --- Messages / Live Chat ---
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)

    fun getAllMessagesForUser(userId: String): Flow<List<MessageEntity>> = messageDao.getAllMessagesForUser(userId)

    suspend fun insertMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        messageDao.insertMessage(message)
    }

    suspend fun markChatAsSeen(chatId: String, userId: String) = withContext(Dispatchers.IO) {
        messageDao.markChatAsSeen(chatId, userId)
    }


    // --- Notifications ---
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUser(userId)

    suspend fun insertNotification(notification: NotificationEntity) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markNotificationsRead(userId: String) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun deleteNotification(id: Int) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotification(id)
    }


    // --- Verification KYC ---
    fun getAllVerificationRequests(): Flow<List<VerificationRequestEntity>> = verificationRequestDao.getAllRequests()

    suspend fun getRequestByUserId(userId: String): VerificationRequestEntity? = withContext(Dispatchers.IO) {
        verificationRequestDao.getRequestByUserId(userId)
    }

    suspend fun insertVerificationRequest(request: VerificationRequestEntity) = withContext(Dispatchers.IO) {
        verificationRequestDao.insertRequest(request)
    }

    suspend fun updateVerificationRequest(request: VerificationRequestEntity) = withContext(Dispatchers.IO) {
        verificationRequestDao.updateRequest(request)
    }


    // --- Transactions (Wallet) ---
    fun getTransactionsForUser(userId: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsForUser(userId)

    suspend fun insertTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }


    // --- Reviews ---
    fun getReviewsForProduct(productId: Int): Flow<List<ReviewEntity>> = reviewDao.getReviewsForProduct(productId)

    suspend fun insertReview(review: ReviewEntity) = withContext(Dispatchers.IO) {
        reviewDao.insertReview(review)
    }


    // --- Database Seeding ---
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        // Check if sellers are present
        val sellers = userDao.getTopSellers().first()
        if (sellers.isEmpty()) {
            // Seed current testing user (The active account buyers/sellers can test with)
            val currentUser = UserEntity(
                userId = "current_user_id",
                username = "Alex Dev",
                email = "alex.dev@prime.io",
                bio = "Full-stack developer building futuristic Android applications. Let's trade digital goods!",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200",
                verifiedBadge = false,
                sellerLevel = "Beginner",
                totalSales = 0,
                rating = 0.0f,
                walletBalance = 500.0, // Preloaded with money for checkout testing
                joinedDate = "May 2026"
            )
            userDao.insertUser(currentUser)

            // Seed premium/verified sellers
            val seller1 = UserEntity(
                userId = "seller_devesh",
                username = "Devesh (Creator)",
                email = "devesh@prime.io",
                bio = "Official developer of Prime Marketplace. Expert in Kotlin, Android Architecture, and web3 tech.",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&q=80&w=200",
                verifiedBadge = true,
                sellerLevel = "Verified Elite",
                totalSales = 1250,
                rating = 4.9f,
                walletBalance = 15840.0,
                joinedDate = "Jan 2024"
            )
            userDao.insertUser(seller1)

            val seller2 = UserEntity(
                userId = "seller_sara",
                username = "Sara Dev Studio",
                email = "sara.studio@gmail.com",
                bio = "Crafting high-quality designs, Notion planners, and custom Python automations.",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
                verifiedBadge = true,
                sellerLevel = "Premium",
                totalSales = 340,
                rating = 4.8f,
                walletBalance = 4220.0,
                joinedDate = "Aug 2024"
            )
            userDao.insertUser(seller2)

            val seller3 = UserEntity(
                userId = "seller_neo",
                username = "NeoTech Labs",
                email = "contact@neotech.io",
                bio = "Next-generation gaming tools, optimization scripts, and performance patches for Android.",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
                verifiedBadge = false,
                sellerLevel = "Trusted",
                totalSales = 185,
                rating = 4.6f,
                walletBalance = 1290.0,
                joinedDate = "Nov 2025"
            )
            userDao.insertUser(seller3)

            // Seed Products
            val products = listOf(
                ProductEntity(
                    title = "Futuristic Banking App UI Kit",
                    description = "A complete, interactive, and premium banking application UI kit designed entirely in modern Jetpack Compose. Includes glowing glowing charts, biometric login, animated cashflow pages, multi-currency wallets, and sleek cyberpunk dark glassmorphism layouts. Over 15 fully coded screens ready to copy-paste into your production apps.",
                    price = 49.99,
                    discountedPrice = 29.99,
                    category = "Source codes",
                    imageUrls = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&q=80&w=500,https://images.unsplash.com/photo-1616077168079-7e09a677fb2c?auto=format&fit=crop&q=80&w=500",
                    fileSize = "18.4 MB",
                    compatibility = "Android SDK 26+ (Compose 1.5+)",
                    versionHistory = "v1.2.0 (Added dynamic charts & dark theme toggles)",
                    tags = "Compose, UI Kit, Fintech, Dark Neon, Glassmorphism",
                    rating = 4.9f,
                    ratingCount = 28,
                    downloadsCount = 142,
                    sellerId = "seller_devesh",
                    isFeatured = true,
                    isTrending = true
                ),
                ProductEntity(
                    title = "AI Auto-Moderation Telegram Script",
                    description = "A production-ready Telegram helper script written in Python. Uses Google's Gemini-1.5-Flash model to live-monitor multiple groups, filter spam and crypto-scams automatically, detect toxicity, generate real-time post summaries, and answer group member requests inline directly. Easy to host on Heroku, Render, or Docker.",
                    price = 19.99,
                    discountedPrice = 14.50,
                    category = "Scripts",
                    imageUrls = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&q=80&w=500",
                    fileSize = "2.1 MB",
                    compatibility = "Python 3.9+",
                    versionHistory = "v2.0.4 (Migrated to latest google-genai SDK)",
                    tags = "AI, Telegram, Python, Bot, Gemini, Moderation",
                    rating = 4.7f,
                    ratingCount = 15,
                    downloadsCount = 89,
                    sellerId = "seller_devesh",
                    isFeatured = true,
                    isTrending = false
                ),
                ProductEntity(
                    title = "Crosshair Elite Overlay Tool",
                    description = "A lightweight mobile gaming utility that draws an aesthetic, fully customizable high-contrast crosshair overlay on your phone or tablet screen. Designed especially for competitive shooting titles. Features customizable outlines, opacity, styles, dynamic sizing based on gyroscope, and custom canvas rendering designed to not block touch interactions.",
                    price = 4.99,
                    discountedPrice = 0.0,
                    category = "Gaming tools",
                    imageUrls = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&q=80&w=500",
                    fileSize = "4.2 MB",
                    compatibility = "Android 7.0+",
                    versionHistory = "v1.0.1 (Initial Launch)",
                    tags = "Gaming, Hack, Overlay, Tool, Android UT",
                    rating = 4.5f,
                    ratingCount = 37,
                    downloadsCount = 210,
                    sellerId = "seller_neo",
                    isFeatured = false,
                    isTrending = true
                ),
                ProductEntity(
                    title = "Premium Notion Solopreneur Life Planner",
                    description = "The ultimate Notion system to structure and coordinate your professional freelance/solopreneur life. Tracks multiple ongoing client contracts, project lifecycles, active download subscriptions, monthly invoices, expense ledgers, social content queues, and product roadmaps elegantly in one workspace. Visually optimized for dark mode Notion users.",
                    price = 24.99,
                    discountedPrice = 12.00,
                    category = "Templates",
                    imageUrls = "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&q=80&w=500",
                    fileSize = "1.2 MB (Notion Import Link)",
                    compatibility = "Notion App (Web, Desktop, iOS, Android)",
                    versionHistory = "v3.1.0",
                    tags = "Notion, Solopreneur, Productivity, Tracker, Freelance",
                    rating = 4.9f,
                    ratingCount = 42,
                    downloadsCount = 312,
                    sellerId = "seller_sara",
                    isFeatured = true,
                    isTrending = true
                ),
                ProductEntity(
                    title = "Cyberpunk Desktop Wallpaper Assets",
                    description = "A pack of 30 UHD (8K) premium digital generated wallpapers capturing futuristic megacities, dark alleyways, cybernetic hackers, and geometric neon architecture. Designed with rich colors and sharp details, perfectly sized for dual monitors, tablets, and mobile screens.",
                    price = 9.99,
                    discountedPrice = 5.00,
                    category = "Designs",
                    imageUrls = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&q=80&w=500",
                    fileSize = "185 MB (Wallpaper ZIP archive)",
                    compatibility = "Any digital display (UHD)",
                    versionHistory = "v1.0 (Released 30 High-Res wallpapers)",
                    tags = "Design, Digital Art, Desktop, Minimal, Cyberpunk",
                    rating = 4.4f,
                    ratingCount = 8,
                    downloadsCount = 45,
                    sellerId = "seller_sara",
                    isFeatured = false,
                    isTrending = false
                ),
                ProductEntity(
                    title = "Mastering Kotlin Coroutines & Flow E-Book",
                    description = "The ultimate 300-page interactive e-book designed to take you from a complete novice to an absolute master of Kotlin Coroutines, asynchronous flows, channels, StateFlows, structured concurrency, and advanced thread dispatching patterns in production. Includes full visual breakdowns and 50+ downloadable sample playgrounds.",
                    price = 29.99,
                    discountedPrice = 19.99,
                    category = "E-books",
                    imageUrls = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=500",
                    fileSize = "12.8 MB (PDF / EPUB format)",
                    compatibility = "Any E-Reader, PDF, EPUB viewer",
                    versionHistory = "v2.1.0 (Updated to support Kotlin 2.x)",
                    tags = "Kotlin, Coroutines, Flow, Threading, Android, Ebook",
                    rating = 4.8f,
                    ratingCount = 92,
                    downloadsCount = 582,
                    sellerId = "seller_devesh",
                    isFeatured = true,
                    isTrending = true
                )
            )

            for (p in products) {
                productDao.insertProduct(p)
            }

            // Seed Reviews for some products
            val reviews = listOf(
                ReviewEntity(
                    productId = 1,
                    userId = "reviewer_jack",
                    userName = "Jack Coder",
                    userAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200",
                    rating = 5,
                    comment = "This is hands-down the cleanest Jetpack Compose code I've ever purchased! The glassmorphism effects look brilliant in the emulator.",
                    replyFromSeller = "Wow, thanks Jack! Glad you liked the glass styling. Expect more widgets in v1.3!",
                    timestamp = System.currentTimeMillis() - 86400000 * 3
                ),
                ReviewEntity(
                    productId = 1,
                    userId = "reviewer_linda",
                    userName = "Linda G.",
                    userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                    rating = 5,
                    comment = "Brilliant design patterns. I integrated the customized graph components into my live fintech app within minutes. Real time saver!",
                    replyFromSeller = "",
                    timestamp = System.currentTimeMillis() - 86400000 * 1
                ),
                ReviewEntity(
                    productId = 2,
                    userId = "reviewer_tom",
                    userName = "Thomas AI",
                    userAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&q=80&w=200",
                    rating = 4,
                    comment = "Highly functional script! The toxicity filters are extremely smart and it has saved our Telegram crypto group from thousands of spambots. Dockers support is a major plus.",
                    replyFromSeller = "Thanks Thomas! Make sure to update your API keys to get the fastest inference times.",
                    timestamp = System.currentTimeMillis() - 86400000 * 5
                )
            )

            for (r in reviews) {
                reviewDao.insertReview(r)
            }

            // Seed Notifications
            val notifications = listOf(
                NotificationEntity(
                    userId = "current_user_id",
                    title = "Welcome to Prime!",
                    text = "Discover pre-loaded digital products, check out our neon dashboard, and join our elite selling system.",
                    type = "order",
                    timestamp = System.currentTimeMillis()
                ),
                NotificationEntity(
                    userId = "current_user_id",
                    title = "Testing Funds pre-loaded",
                    text = "We have preloaded your test wallet with $500.00 so you can try ordering digital items safely!",
                    type = "sale",
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )

            for (n in notifications) {
                notificationDao.insertNotification(n)
            }

            // Seed transactional history for current user
            val transactions = listOf(
                TransactionEntity(
                    transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                    userId = "current_user_id",
                    amount = 500.0,
                    type = "DEPOSIT",
                    description = "Preloaded testing allowance",
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                )
            )

            for (t in transactions) {
                transactionDao.insertTransaction(t)
            }
        }
    }
}
