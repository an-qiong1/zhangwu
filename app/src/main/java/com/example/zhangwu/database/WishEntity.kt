package com.example.zhangwu.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 心愿单 Room 实体
 * 对应 UI 模型 [com.example.zhangwu.model.WishItem]
 */
@Entity(tableName = "wishes")
data class WishEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val targetDate: String,
    val isPurchased: Boolean = false,
    val icon: String = "❤️",
    val remark: String = "",
    val imageUri: String = ""
)
