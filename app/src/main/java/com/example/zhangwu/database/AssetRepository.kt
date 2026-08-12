package com.example.zhangwu.database

import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.toAsset
import com.example.zhangwu.model.toAssetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AssetRepository(private val assetDao: AssetDao) {

    fun getAllAssets(): Flow<List<Asset>> {
        return assetDao.getAllAssets().map { entities ->
            entities.map { it.toAsset() }
        }
    }

    fun getAssetsByCategory(category: String): Flow<List<Asset>> {
        return assetDao.getAssetsByCategory(category).map { entities ->
            entities.map { it.toAsset() }
        }
    }

    suspend fun insertAsset(asset: Asset) {
        assetDao.insertAsset(asset.toAssetEntity())
    }

    suspend fun updateAsset(asset: Asset) {
        assetDao.updateAsset(asset.toAssetEntity())
    }

    suspend fun deleteAsset(asset: Asset) {
        assetDao.deleteAsset(asset.toAssetEntity())
    }

    suspend fun deleteAll() {
        assetDao.deleteAll()
    }

    /**
     * 事务化批量替换（用于从备份恢复）
     * 内部走 Dao 的 @Transaction，失败回滚，原数据保留
     */
    suspend fun replaceAll(assets: List<Asset>) {
        assetDao.replaceAllAssets(assets.map { it.toAssetEntity() })
    }
}