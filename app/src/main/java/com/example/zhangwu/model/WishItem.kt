package com.example.zhangwu.model

import androidx.compose.runtime.Immutable

/**
 * 心愿数据类
 * @property id 唯一标识符（稳定的 Long id）
 * @property name 心愿名称
 * @property price 目标价格（统一为 Double）
 * @property targetDate 目标日期
 * @property isPurchased 是否已购买（替代原 savedAmount 攒钱进度的"完成"判断）
 * @property icon 图标
 * @property remark 备注
 * @property imageUri 图片URI
 */
@Immutable
data class WishItem(
    // id=0 表示新增项，由 Room 数据库自增分配真实 id
    val id: Long = 0L,
    val name: String,
    val price: Double,
    val targetDate: String,
    val isPurchased: Boolean = false,
    val icon: String = "❤️",
    val remark: String = "",
    val imageUri: String = ""
)
