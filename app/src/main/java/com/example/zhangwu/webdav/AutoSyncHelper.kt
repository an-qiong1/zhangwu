package com.example.zhangwu.webdav

import android.content.Context
import android.util.Log
import com.example.zhangwu.database.AppDatabase
import com.example.zhangwu.database.toWishItem
import com.example.zhangwu.model.toAsset
import kotlinx.coroutines.flow.first

/**
 * 自动同步助手：进入 APP 时静默下载、退出 APP 时自动上传
 * 由 ProcessLifecycleObserver 调用
 */
class AutoSyncHelper(private val context: Context) {
    private val syncManager = WebDavSyncManager(context)
    private val db by lazy { AppDatabase.getDatabase(context) }

    /**
     * 退出 APP 时自动上传：从数据库读取资产+心愿+分类，上传到坚果云
     */
    suspend fun autoUpload() {
        try {
            val config = syncManager.getConfig()
            if (!config.isConfigured) return  // 未配置则跳过

            val assets = db.assetDao().getAllAssets().first().map { it.toAsset() }
            val wishes = db.wishDao().getAllWishes().first().map { it.toWishItem() }
            val categories = db.categoryDao().getAllCategoryNames()

            when (val r = syncManager.upload(assets, wishes, categories)) {
                is WebDavResult.Success -> Log.d("AutoSync", "自动上传成功")
                is WebDavResult.Error -> Log.w("AutoSync", "自动上传失败: ${r.message}")
            }
        } catch (e: Exception) {
            Log.w("AutoSync", "自动上传异常: ${e.message}")
        }
    }

    /**
     * 进入 APP 时：如果已配置但从未同步过，静默拉取一次云端备份
     * 不自动覆盖本地（避免数据丢失），仅更新 lastSyncTime 标记
     * 用户需手动去备份页面点「从坚果云恢复」
     */
    suspend fun checkFirstSync(): Boolean {
        return try {
            val config = syncManager.getConfig()
            if (!config.isConfigured) return false

            val lastSync = syncManager.getLastSyncTime()
            // 如果本地从未同步过，且云端有备份，提示用户可以恢复
            if (lastSync.isBlank()) {
                when (val r = syncManager.download()) {
                    is WebDavResult.Success -> {
                        // 云端有备份，但本地为空——提示用户手动恢复
                        // 这里不自动恢复，避免覆盖用户刚输入的数据
                        Log.d("AutoSync", "云端有备份，提示用户手动恢复")
                        return true
                    }
                    is WebDavResult.Error -> {
                        Log.d("AutoSync", "云端无备份: ${r.message}")
                        return false
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.w("AutoSync", "首次同步检查异常: ${e.message}")
            false
        }
    }
}
