package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.AssetStatus
import com.example.domain.model.OperationType
import com.example.domain.model.SyncStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole

/**
 * Type converters for storing custom Kotlin enums into SQLite database tables.
 */
class DatabaseConverters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.WAREHOUSE_EMPLOYEE)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.STOCK_IN)

    @TypeConverter
    fun fromAssetStatus(value: AssetStatus): String = value.name

    @TypeConverter
    fun toAssetStatus(value: String): AssetStatus = runCatching { AssetStatus.valueOf(value) }.getOrDefault(AssetStatus.AVAILABLE)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = runCatching { SyncStatus.valueOf(value) }.getOrDefault(SyncStatus.PENDING)

    @TypeConverter
    fun fromOperationType(value: OperationType): String = value.name

    @TypeConverter
    fun toOperationType(value: String): OperationType = runCatching { OperationType.valueOf(value) }.getOrDefault(OperationType.CREATE)
}
