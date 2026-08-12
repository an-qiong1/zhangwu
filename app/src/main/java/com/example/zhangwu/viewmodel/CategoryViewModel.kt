package com.example.zhangwu.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhangwu.database.AppDatabase
import com.example.zhangwu.database.CategoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import android.app.Application

/**
 * 分类ViewModel
 * 从 Room 数据库读写分类，持久化存储，APP 重启不丢失
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).categoryDao()

    val defaultCategories = listOf("全部")

    // 从数据库订阅分类列表 + 去重（双重保险：即便数据库有重复脏数据也能过滤）
    private val _dbCategories = dao.getAllCategories()
        .map { entities -> entities.map { it.name }.distinct() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 合并后的分类列表（默认"全部" + 数据库中的分类，并过滤掉重复的默认分类防止key重复闪退）
    val categoryList: StateFlow<List<String>> = _dbCategories
        .map { dbList ->
            val filtered = dbList.filter { !defaultCategories.contains(it) }
            (listOf("全部") + filtered).distinct()
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf("全部"))

    // 分类选择状态
    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            if (categoryName.isBlank()) return@launch
            val current = categoryList.value
            if (current.contains(categoryName)) return@launch
            val nextOrder = (dao.getAllCategoryNames().size)
            dao.insertCategory(CategoryEntity(name = categoryName, sortOrder = nextOrder))
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            if (defaultCategories.contains(category)) return@launch
            dao.deleteCategoryByName(category)
            if (_selectedCategory.value == category) {
                _selectedCategory.value = "全部"
            }
        }
    }

    fun reorderCategories(newOrder: List<String>) {
        viewModelScope.launch {
            val customList = newOrder.filter { !defaultCategories.contains(it) }
            customList.forEachIndexed { index, name ->
                dao.updateSortOrderByName(name, index)
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun isDefaultCategory(category: String): Boolean {
        return defaultCategories.contains(category)
    }

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            if (newName.isBlank()) return@launch
            dao.renameCategory(oldName, newName)
            dao.updateAssetCategory(oldName, newName)
            if (_selectedCategory.value == oldName) {
                _selectedCategory.value = newName
            }
        }
    }

    fun restoreCategories(categories: List<String>) {
        viewModelScope.launch {
            val customList = categories.filter { it.isNotBlank() && !defaultCategories.contains(it) }
            customList.forEachIndexed { index, name ->
                dao.insertCategory(CategoryEntity(name = name, sortOrder = index))
            }
        }
    }
}
