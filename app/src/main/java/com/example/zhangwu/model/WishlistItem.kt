package com.example.zhangwu.model

/**
 * UI model for wishlist item
 */
data class WishlistItem(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val category: String = "",
    val icon: String = "🎁",
    val remark: String = "",
    val imageUri: String = ""
)