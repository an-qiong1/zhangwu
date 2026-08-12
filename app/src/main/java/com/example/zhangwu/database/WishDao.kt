package com.example.zhangwu.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WishDao {
    @Query("SELECT * FROM wishes ORDER BY id DESC")
    fun getAllWishes(): Flow<List<WishEntity>>

    @Query("SELECT * FROM wishes WHERE id = :id LIMIT 1")
    suspend fun getWishById(id: Long): WishEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWish(wish: WishEntity): Long

    @Update
    suspend fun updateWish(wish: WishEntity)

    @Delete
    suspend fun deleteWish(wish: WishEntity)

    @Query("DELETE FROM wishes WHERE id = :id")
    suspend fun deleteWishById(id: Long)

    @Query("DELETE FROM wishes")
    suspend fun deleteAll()

    /**
     * 事务化批量替换：先清空再批量插入，整体在单个事务内执行
     * 任意一条插入失败会回滚整个事务，原数据保留，避免恢复中途失败导致空表
     */
    @Transaction
    suspend fun replaceAllWishes(wishes: List<WishEntity>) {
        deleteAll()
        wishes.forEach { insertWish(it) }
    }
}
