package com.example.zhangwu.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY purchaseDate DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE category = :category ORDER BY purchaseDate DESC")
    fun getAssetsByCategory(category: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets")
    suspend fun deleteAll()

    /**
     * 事务化批量替换：先清空再批量插入，整体在单个事务内执行
     * 任意一条插入失败会回滚整个事务，原数据保留，避免恢复中途失败导致空表
     */
    @Transaction
    suspend fun replaceAllAssets(assets: List<AssetEntity>) {
        deleteAll()
        assets.forEach { insertAsset(it) }
    }
}