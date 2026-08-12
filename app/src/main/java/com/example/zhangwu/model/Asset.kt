package com.example.zhangwu.model

import androidx.compose.runtime.Immutable
import com.example.zhangwu.database.AssetEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * UI model for asset, compatible with existing composables.
 * Contains stored fields (purchasePrice, purchaseDate, expectedYears) and computed fields.
 */
@Immutable
data class Asset(
    val id: Int = 0,
    val name: String = "",
    val purchasePrice: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis(),
    val expectedYears: Int = 0, // 0 = 未设置预期使用年限
    val category: String = "未分类",
    val status: String = "服役中",
    val sellDate: Long? = null,
    val sellPrice: Double? = null,
    val remark: String = "",
    val imageUri: String? = null,
    val tags: List<String> = emptyList()
) {
    // 是否设置了预期使用年限
    val hasExpectedYears: Boolean
        get() = expectedYears > 0

    // 价格格式化字符串：整数不保留小数，非整数最多1位
    val price: String
        get() {
            val longVal = purchasePrice.toLong()
            return if (purchasePrice == longVal.toDouble()) {
                "¥$longVal"
            } else {
                "¥${"%.1f".format(purchasePrice)}"
            }
        }

    // 总天数：expectedYears=0 时返回 0
    val totalDays: Int
        get() = if (expectedYears <= 0) 0 else expectedYears * 365

    // 剩余天数：依赖 usedDays，调用方应缓存快照
    val remainingDays: Int
        get() = maxOf(0, totalDays - usedDays())

    // 已使用天数：改为函数，调用方负责缓存
    fun usedDays(now: Long = System.currentTimeMillis()): Int {
        val diff = now - purchaseDate
        return (diff / (1000L * 60 * 60 * 24)).toInt()
    }

    // 进度：纯计算，未设置年限时返回 0
    fun progress(now: Long = System.currentTimeMillis()): Float {
        if (totalDays == 0) return 0f
        return usedDays(now).toFloat() / totalDays
    }

    // 日均成本：基于实际使用天数（购入价格 ÷ 已使用天数），始终显示
    // 方案B — <0.1 显示2位小数，≥0.1 最多1位，整数无小数
    fun dailyCost(now: Long = System.currentTimeMillis()): String {
        val days = usedDays(now)
        if (days <= 0) return "¥0"
        val daily = purchasePrice / days
        val longVal = daily.toLong()
        return if (daily == longVal.toDouble()) {
            "¥$longVal"
        } else if (daily < 0.1) {
            "¥${"%.2f".format(daily)}"
        } else {
            "¥${"%.1f".format(daily)}"
        }
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