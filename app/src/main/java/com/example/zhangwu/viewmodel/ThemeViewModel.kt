package com.example.zhangwu.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// DataStore扩展属性
private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

/**
 * 主题ViewModel
 * 负责管理应用的主题状态，支持跟随系统、浅色模式、深色模式
 * 使用DataStore实现主题状态持久化
 */
class ThemeViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    // 主题模式枚举
    enum class ThemeMode {
        FOLLOW_SYSTEM,
        LIGHT,
        DARK
    }
    
    // 主题模式状态
    private val _themeMode = MutableStateFlow(ThemeMode.FOLLOW_SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    // 主题模式存储键
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    
    init {
        // 初始化时从DataStore读取主题状态
        loadThemeMode()
    }
    
    /**
     * 从DataStore加载主题状态
     */
    private fun loadThemeMode() {
        viewModelScope.launch {
            try {
                val preferences = context.themeDataStore.data.first()
                val savedMode = preferences[THEME_MODE_KEY]
                val mode = when (savedMode) {
                    "FOLLOW_SYSTEM" -> ThemeMode.FOLLOW_SYSTEM
                    "LIGHT" -> ThemeMode.LIGHT
                    "DARK" -> ThemeMode.DARK
                    else -> ThemeMode.FOLLOW_SYSTEM
                }
                _themeMode.value = mode
            } catch (e: Exception) {
                // 加载失败时使用默认值
                _themeMode.value = ThemeMode.FOLLOW_SYSTEM
            }
        }
    }
    
    /**
     * 设置主题模式
     * @param mode 主题模式
     */
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        // 保存主题状态到DataStore
        saveThemeMode(mode)
    }
    
    /**
     * 保存主题状态到DataStore
     * @param mode 主题模式
     */
    private fun saveThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                context.themeDataStore.edit {
                    it[THEME_MODE_KEY] = mode.name
                }
            } catch (e: Exception) {
                // 保存失败时忽略
            }
        }
    }
    
    /**
     * 获取当前是否为深色模式
     * @param isSystemInDarkTheme 系统是否为深色模式
     * @return 是否为深色模式
     */
    fun isDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
        return when (_themeMode.value) {
            ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }
}