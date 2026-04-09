package com.example.zhangwu.model

import java.util.*

/**
 * 心愿数据类
 * @property id 唯一标识符
 * @property name 心愿名称
 * @property price 目标价格
 * @property targetDate 目标日期
 * @property savedAmount 已存金额
 * @property targetAmount 目标金额
 * @property icon 图标
 * @property remark 备注
 * @property imageUri 图片URI
 */
data class WishItem(
    val id: Int = UUID.randomUUID().hashCode(),
    val name: String,
    val price: String,
    val targetDate: String,
    val savedAmount: Double = 0.0,
    val targetAmount: Double,
    val icon: String = "❤️",
    val remark: String = "",
    val imageUri: String = ""
) {
    // 计算攒钱进度
    val progress: Float
        get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat() else 0f
    
    // 计算剩余金额
    val remainingAmount: Double
        get() = targetAmount - savedAmount
}
