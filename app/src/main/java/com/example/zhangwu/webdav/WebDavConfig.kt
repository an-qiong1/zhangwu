package com.example.zhangwu.webdav

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * WebDAV 配置（坚果云）
 */
data class WebDavConfig(
    val serverUrl: String = "https://dav.jianguoyun.com/dav/",
    val username: String = "",
    val password: String = "",  // 坚果云应用专用密码
    val remotePath: String = "/zhangwu_backup/"  // 远程备份目录
) {
    /** 配置是否完整可用 */
    val isConfigured: Boolean get() = username.isNotBlank() && password.isNotBlank()
}

private val Context.webDavDataStore: DataStore<Preferences> by preferencesDataStore(name = "webdav_prefs")

/**
 * WebDAV 配置仓库：基于 DataStore 持久化
 */
class WebDavConfigRepository(private val context: Context) {
    private object Keys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val REMOTE_PATH = stringPreferencesKey("remote_path")
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
    }

    val configFlow: Flow<WebDavConfig> = context.webDavDataStore.data.map { prefs ->
        WebDavConfig(
            serverUrl = prefs[Keys.SERVER_URL] ?: "https://dav.jianguoyun.com/dav/",
            username = prefs[Keys.USERNAME] ?: "",
            password = prefs[Keys.PASSWORD] ?: "",
            remotePath = prefs[Keys.REMOTE_PATH] ?: "/zhangwu_backup/"
        )
    }

    val lastSyncTimeFlow: Flow<String> = context.webDavDataStore.data.map { prefs ->
        prefs[Keys.LAST_SYNC_TIME] ?: ""
    }

    suspend fun saveConfig(config: WebDavConfig) {
        context.webDavDataStore.edit { prefs ->
            prefs[Keys.SERVER_URL] = config.serverUrl
            prefs[Keys.USERNAME] = config.username
            prefs[Keys.PASSWORD] = config.password
            prefs[Keys.REMOTE_PATH] = config.remotePath
        }
    }

    suspend fun updateLastSyncTime(timeStr: String) {
        context.webDavDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = timeStr
        }
    }
}
