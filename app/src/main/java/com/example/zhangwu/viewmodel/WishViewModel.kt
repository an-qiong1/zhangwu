package com.example.zhangwu.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhangwu.model.WishItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 心愿ViewModel
 * 负责管理心愿数据的状态和逻辑
 */
class WishViewModel : ViewModel() {
    // 修复：使用mutableStateListOf确保数据变化触发UI重组
    private val _wishItems = mutableStateListOf<WishItem>()
    val wishItems: List<WishItem> get() = _wishItems
    
    init {
        // 修复：清除默认模拟数据，心愿单初始为空
        // initMockData()
    }
    
    /**
     * 初始化模拟数据
     */
    private fun initMockData() {
        val mockWishes = listOf(
            WishItem(
                name = "iPhone 15 Pro",
                price = "7999",
                targetDate = "2026-12-31",
                savedAmount = 3000.0,
                targetAmount = 7999.0
            ),
            WishItem(
                name = "MacBook Pro",
                price = "14999",
                targetDate = "2026-12-31",
                savedAmount = 5000.0,
                targetAmount = 14999.0
            ),
            WishItem(
                name = "Tesla Model 3",
                price = "239900",
                targetDate = "2027-12-31",
                savedAmount = 50000.0,
                targetAmount = 239900.0
            ),
            WishItem(
                name = "Apple Watch Ultra",
                price = "6299",
                targetDate = "2026-12-31",
                savedAmount = 2000.0,
                targetAmount = 6299.0
            )
        )
        
        // 添加模拟数据到列表
        _wishItems.addAll(mockWishes)
    }
    
    /**
     * 添加心愿
     * @param wishItem 新的心愿项
     */
    fun addWishItem(wishItem: WishItem) {
        viewModelScope.launch {
            _wishItems.add(wishItem)
        }
    }
    
    /**
     * 删除心愿
     * @param wishItem 要删除的心愿项
     */
    fun deleteWishItem(wishItem: WishItem) {
        viewModelScope.launch {
            _wishItems.remove(wishItem)
        }
    }
    
    /**
     * 更新心愿
     * @param wishItem 更新后的心愿项
     */
    fun updateWishItem(wishItem: WishItem) {
        viewModelScope.launch {
            val index = _wishItems.indexOfFirst { it.id == wishItem.id }
            if (index != -1) {
                _wishItems[index] = wishItem
            }
        }
    }
}
