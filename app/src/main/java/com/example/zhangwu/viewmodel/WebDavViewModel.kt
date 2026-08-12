package com.example.zhangwu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhangwu.BackupData
import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.WishItem
import com.example.zhangwu.webdav.WebDavConfig
import com.example.zhangwu.webdav.WebDavResult
import com.example.zhangwu.webdav.WebDavSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * WebDAV 同步 ViewModel
 * 管理坚果云配置、上传/下载、同步状态
 */
class WebDavViewModel(application: Application) : AndroidViewModel(application) {
    private val syncManager = WebDavSyncManager(application)

    private val _config = MutableStateFlow(WebDavConfig())
    val config: StateFlow<WebDavConfig> = _config.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    // 同步状态：idle / syncing / success / error
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        // 加载已保存的配置和上次同步时间
        viewModelScope.launch {
            syncManager.getConfig().let { _config.value = it }
            syncManager.getLastSyncTime().let { _lastSyncTime.value = it }
        }
    }

    fun updateConfig(newConfig: WebDavConfig) {
        viewModelScope.launch {
            syncManager.saveConfig(newConfig)
            _config.value = newConfig
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing("测试连接中...")
            when (val r = syncManager.testConnection()) {
                is WebDavResult.Success -> _syncState.value = SyncState.Success("连接成功")
                is WebDavResult.Error -> _syncState.value = SyncState.Error(r.message)
            }
        }
    }

    fun upload(assets: List<Asset>, wishes: List<WishItem>, categories: List<String>) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing("正在上传到坚果云...")
            when (val r = syncManager.upload(assets, wishes, categories)) {
                is WebDavResult.Success -> {
                    _lastSyncTime.value = syncManager.getLastSyncTime()
                    _syncState.value = SyncState.Success("上传成功")
                }
                is WebDavResult.Error -> _syncState.value = SyncState.Error(r.message)
            }
        }
    }

    /**
     * 下载备份（不自动恢复，由 UI 层确认后调用 onDownloaded 回调）
     */
    fun download(onDownloaded: (BackupData) -> Unit) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing("正在从坚果云下载...")
            when (val r = syncManager.download()) {
                is WebDavResult.Success -> {
                    _syncState.value = SyncState.Success("下载成功，请确认恢复")
                    onDownloaded(r.data)
                }
                is WebDavResult.Error -> _syncState.value = SyncState.Error(r.message)
            }
        }
    }

    fun resetState() {
        _syncState.value = SyncState.Idle
    }

    sealed class SyncState {
        object Idle : SyncState()
        data class Syncing(val message: String) : SyncState()
        data class Success(val message: String) : SyncState()
        data class Error(val message: String) : SyncState()
    }
}
