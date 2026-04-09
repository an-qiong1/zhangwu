package com.example.zhangwu.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 分类ViewModel
 * 负责管理分类数据的状态和逻辑，实现资产页分类栏与分类管理页面的数据双向同步
 */
class CategoryViewModel : ViewModel() {
    // 初始默认分类（锁定，不可删除）
    val defaultCategories = listOf("全部")
    
    // 修复：使用mutableStateListOf确保数据变化触发UI重组
    private val _categoryList = mutableStateListOf<String>()
    val categoryList: List<String> get() = _categoryList
    
    // 分类选择状态
    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    init {
        // 初始化分类列表，添加默认分类
        _categoryList.addAll(defaultCategories)
        // 添加一些默认分类示例
        _categoryList.addAll(listOf("享界", "模型"))
    }
    
    /**
     * 添加分类
     * @param categoryName 新分类名称
     */
    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            if (categoryName.isBlank() || _categoryList.contains(categoryName)) {
                return@launch
            }
            _categoryList.add(categoryName)
        }
    }
    
    /**
     * 删除分类
     * @param category 要删除的分类
     */
    fun deleteCategory(category: String) {
        viewModelScope.launch {
            // 默认分类不可删除
            if (!defaultCategories.contains(category)) {
                _categoryList.remove(category)
                // 如果删除的是当前选中的分类，切换到"全部"
                if (_selectedCategory.value == category) {
                    _selectedCategory.value = "全部"
                }
            }
        }
    }
    
    /**
     * 重新排序分类
     * @param newOrder 新的分类顺序
     */
    fun reorderCategories(newOrder: List<String>) {
        viewModelScope.launch {
            // 保留默认分类在最前面
            val defaultList = defaultCategories.toMutableList()
            val customList = newOrder.filter { !defaultCategories.contains(it) }
            _categoryList.clear()
            _categoryList.addAll(defaultList + customList)
        }
    }
    
    /**
     * 选择分类
     * @param category 选中的分类
     */
    fun selectCategory(category: String) {
        viewModelScope.launch {
            _selectedCategory.value = category
        }
    }
    
    /**
     * 检查分类是否为默认分类
     * @param category 分类名称
     * @return 是否为默认分类
     */
    fun isDefaultCategory(category: String): Boolean {
        return defaultCategories.contains(category)
    }
}