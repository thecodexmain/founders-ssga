package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        VerificationRequestEntity::class,
        TransactionEntity::class,
        ReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun verificationRequestDao(): VerificationRequestDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reviewDao(): ReviewDao
}
