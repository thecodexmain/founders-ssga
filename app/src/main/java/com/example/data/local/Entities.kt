package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val email: String,
    val bio: String,
    val avatarUrl: String,
    val verifiedBadge: Boolean = false,
    val sellerLevel: String = "Beginner", // Beginner, Trusted, Premium, Verified Elite
    val totalSales: Int = 0,
    val rating: Float = 0.0f,
    val followersCount: Int = 0,
    val socialLinks: String = "", // comma-separated or json string
    val walletBalance: Double = 0.0,
    val joinedDate: String = "May 2026",
    val followingIds: String = "" // comma-separated list of seller userids
) : Serializable

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val productId: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val discountedPrice: Double = 0.0,
    val category: String, // APK, Script, Course, Template, Gaming Tool, E-book
    val imageUrls: String, // Comma separated URLs
    val previewVideoUrl: String = "",
    val downloadUrl: String = "",
    val fileSize: String = "0 MB",
    val compatibility: String = "Android 8.0+",
    val versionHistory: String = "v1.0.0",
    val tags: String = "", // Comma separated tags
    val rating: Float = 5.0f,
    val ratingCount: Int = 1,
    val downloadsCount: Int = 0,
    val sellerId: String,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val productId: Int,
    val productName: String,
    val buyerId: String,
    val sellerId: String,
    val amountPaid: Double,
    val licenseKey: String = "",
    val secureDownloadLink: String,
    val downloadsUsed: Int = 0,
    val maxDownloads: Int = 5,
    val isDelivered: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String, // e.g. "buyerId_sellerId"
    val senderId: String,
    val receiverId: String,
    val messageText: String,
    val imageUrl: String = "",
    val attachmentUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val isAudio: Boolean = false
) : Serializable

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val notificationId: Int = 0,
    val userId: String,
    val title: String,
    val text: String,
    val type: String, // message, sale, follower, order, discount
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : Serializable

@Entity(tableName = "verification_requests")
data class VerificationRequestEntity(
    @PrimaryKey val userId: String,
    val realName: String,
    val idProofLink: String,
    val status: String = "PENDING", // PENDING, VERIFIED, REJECTED
    val notes: String = "",
    val requestTimestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val userId: String,
    val amount: Double,
    val type: String, // DEPOSIT, WITHDRAWAL, SALE, PURCHASE, REFUND
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val rating: Int,
    val comment: String,
    val imageUrl: String = "",
    val replyFromSeller: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
