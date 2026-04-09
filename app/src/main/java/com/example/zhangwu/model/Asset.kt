package com.example.zhangwu.model

import com.example.zhangwu.database.AssetEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * UI model for asset, compatible with existing composables.
 * Contains stored fields (purchasePrice, purchaseDate, expectedYears) and computed fields.
 */
data class Asset(
    val id: Int = 0,
    val name: String = "",
    val purchasePrice: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis(),
    val expectedYears: Int = 2,
    val category: String = "未分类",
    val status: String = "服役中",
    val sellDate: Long? = null,
    val sellPrice: Double? = null,
    val remark: String = "",
    val imageUri: String? = null,
    val tags: List<String> = emptyList()
) {
    // Computed properties for UI
    val price: String
        get() = "¥%.2f".format(purchasePrice)

    val usedDays: Int
        get() {
            val purchase = Date(purchaseDate)
            val now = Date()
            val diff = now.time - purchase.time
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

    val totalDays: Int
        get() = expectedYears * 365

    val remainingDays: Int
        get() = maxOf(0, totalDays - usedDays)

    val progress: Float
        get() {
            if (totalDays == 0) return 0f
            return usedDays.toFloat() / totalDays
        }

    val dailyCost: String
        get() {
            if (totalDays == 0) return "¥0.00"
            val daily = purchasePrice / totalDays
            return "¥%.2f".format(daily)
        }
}

// Conversion extensions
fun AssetEntity.toAsset(): Asset {
    val gson = Gson()
    val type = object : TypeToken<List<String>>() {}.type
    val tags = gson.fromJson(this.tags, type) as List<String>
    return Asset(
        id = this.id.toInt(),
        name = this.name,
        purchasePrice = this.purchasePrice,
        purchaseDate = this.purchaseDate,
        expectedYears = this.expectedYears,
        category = this.category,
        status = this.status,
        sellDate = this.sellDate,
        sellPrice = this.sellPrice,
        remark = this.remark,
        imageUri = this.imageUri,
        tags = tags
    )
}

fun Asset.toAssetEntity(): AssetEntity {
    val gson = Gson()
    val tagsJson = gson.toJson(this.tags)
    return AssetEntity(
        id = this.id.toLong(),
        name = this.name,
        purchasePrice = this.purchasePrice,
        purchaseDate = this.purchaseDate,
        expectedYears = this.expectedYears,
        category = this.category,
        status = this.status,
        sellDate = this.sellDate,
        sellPrice = this.sellPrice,
        remark = this.remark,
        imageUri = this.imageUri,
        tags = tagsJson
    )
}