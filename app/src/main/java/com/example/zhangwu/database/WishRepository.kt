package com.example.zhangwu.database

import com.example.zhangwu.model.WishItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WishRepository(private val wishDao: WishDao) {

    fun getAllWishes(): Flow<List<WishItem>> {
        return wishDao.getAllWishes().map { entities ->
            entities.map { it.toWishItem() }
        }
    }

    suspend fun insertWish(wish: WishItem): Long {
        return wishDao.insertWish(wish.toWishEntity())
    }

    suspend fun updateWish(wish: WishItem) {
        wishDao.updateWish(wish.toWishEntity())
    }

    suspend fun deleteWish(wish: WishItem) {
        wishDao.deleteWish(wish.toWishEntity())
    }

    suspend fun deleteAll() {
        wishDao.deleteAll()
    }

    /**
     * 事务化批量替换（用于从备份恢复）
     * 内部走 Dao 的 @Transaction，失败回滚，原数据保留
     */
    suspend fun replaceAll(wishes: List<WishItem>) {
        wishDao.replaceAllWishes(wishes.map { it.toWishEntity() })
    }
}

// ===================== Entity ↔ Model 转换 =====================

fun WishEntity.toWishItem(): WishItem {
    return WishItem(
        id = this.id,
        name = this.name,
        price = this.price,
        targetDate = this.targetDate,
        isPurchased = this.isPurchased,
        icon = this.icon,
        remark = this.remark,
        imageUri = this.imageUri
    )
}

fun WishItem.toWishEntity(): WishEntity {
    return WishEntity(
        id = this.id,
        name = this.name,
        price = this.price,
        targetDate = this.targetDate,
        isPurchased = this.isPurchased,
        icon = this.icon,
        remark = this.remark,
        imageUri = this.imageUri
    )
}
