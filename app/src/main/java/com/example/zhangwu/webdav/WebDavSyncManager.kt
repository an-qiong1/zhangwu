package com.example.zhangwu.webdav

import android.content.Context
import com.example.zhangwu.BackupData
import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.WishItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WebDAV 同步管理器：负责把备份数据上传到坚果云 / 从坚果云下载恢复
 * 复用 [BackupData] 格式，远程文件名固定为 zhangwu_backup.json
 */
class WebDavSyncManager(private val context: Context) {
    private val configRepo = WebDavConfigRepository(context)
    private val gson = Gson()
    private val remoteFileName = "zhangwu_backup.json"

    /** 获取当前配置（一次性读取） */
    suspend fun getConfig(): WebDavConfig = configRepo.configFlow.first()

    /** 保存配置 */
    suspend fun saveConfig(config: WebDavConfig) = configRepo.saveConfig(config)

    /** 获取上次同步时间 */
    suspend fun getLastSyncTime(): String = configRepo.lastSyncTimeFlow.first()

    /** 测试连接 */
    suspend fun testConnection(): WebDavResult<Unit> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (!config.isConfigured) {
            return@withContext WebDavResult.Error("请先填写账号和密码")
        }
        WebDavClient(config).testConnection()
    }

    /**
     * 上传备份到坚果云
     * @param assets 资产列表
     * @param wishes 心愿列表
     * @param categories 分类列表
     */
    suspend fun upload(
        assets: List<Asset>,
        wishes: List<WishItem>,
        categories: List<String>
    ): WebDavResult<Unit> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (!config.isConfigured) {
            return@withContext WebDavResult.Error("请先填写账号和密码")
        }
        val backupData = BackupData(
            assets = assets,
            wishlist = wishes,
            categories = categories,
            backupTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
        )
        val json = gson.toJson(backupData)
        val result = WebDavClient(config).upload(remoteFileName, json.toByteArray())
        if (result is WebDavResult.Success) {
            configRepo.updateLastSyncTime(backupData.backupTime)
        }
        result
    }

    /**
     * 从坚果云下载备份
     * @return 解析后的 BackupData，或错误信息
     */
    suspend fun download(): WebDavResult<BackupData> = withContext(Dispatchers.IO) {
        val config = getConfig()
        if (!config.isConfigured) {
            return@withContext WebDavResult.Error("请先填写账号和密码")
        }
        when (val r = WebDavClient(config).download(remoteFileName)) {
            is WebDavResult.Error -> r
            is WebDavResult.Success -> try {
                val json = String(r.data, Charsets.UTF_8)
                val type = object : TypeToken<BackupData>() {}.type
                val backupData = gson.fromJson<BackupData>(json, type)
                if (backupData == null) {
                    WebDavResult.Error("备份文件解析失败")
                } else {
                    WebDavResult.Success(backupData)
                }
            } catch (e: Exception) {
                WebDavResult.Error("解析备份失败: ${e.message}")
            }
        }
    }
}
