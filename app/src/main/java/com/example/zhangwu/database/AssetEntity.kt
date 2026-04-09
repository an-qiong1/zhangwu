package com.example.zhangwu.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val purchasePrice: Double,
    val purchaseDate: Long, // timestamp in milliseconds
    val expectedYears: Int,
    val category: String,
    val status: String = "服役中",
    val sellDate: Long? = null,
    val sellPrice: Double? = null,
    val remark: String = "",
    val imageUri: String? = null,
    val tags: String = "[]" // JSON string of tags
) {
    // Helper to compute used days based on purchase date
    fun getUsedDays(): Int {
        val purchase = Date(purchaseDate)
        val now = Date()
        val diff = now.time - purchase.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // Total days expected
    fun getTotalDays(): Int = expectedYears * 365

    // Remaining days
    fun getRemainingDays(): Int = maxOf(0, getTotalDays() - getUsedDays())

    // Progress (0..1)
    fun getProgress(): Float {
        val total = getTotalDays()
        if (total == 0) return 0f
        return getUsedDays().toFloat() / total
    }

    // Daily cost string formatted
    fun getDailyCostString(): String {
        val total = getTotalDays()
        if (total == 0) return "¥0.00"
        val daily = purchasePrice / total
        return "¥%.2f".format(daily)
    }

    // Price string formatted
    fun getPriceString(): String = "¥%.2f".format(purchasePrice)
}