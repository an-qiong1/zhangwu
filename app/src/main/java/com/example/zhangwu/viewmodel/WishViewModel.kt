package com.example.zhangwu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhangwu.database.AppDatabase
import com.example.zhangwu.database.AssetRepository
import com.example.zhangwu.database.WishRepository
import com.example.zhangwu.model.Asset
import com.example.zhangwu.model.WishItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 心愿 ViewModel
 * 基于 Room 持久化，APP 重启后数据保留
 */
class WishViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WishRepository
    private val assetRepository: AssetRepository

    // UI 通过 collectAsState() 订阅
    val wishItems: StateFlow<List<WishItem>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WishRepository(db.wishDao())
        assetRepository = AssetRepository(db.assetDao())
        wishItems = repository.getAllWishes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addWishItem(wishItem: WishItem) {
        viewModelScope.launch {
            repository.insertWish(wishItem)
        }
    }

    fun updateWishItem(wishItem: WishItem) {
        viewModelScope.launch {
            repository.updateWish(wishItem)
        }
    }

    fun deleteWishItem(wishItem: WishItem) {
        viewModelScope.launch {
            repository.deleteWish(wishItem)
        }
    }

    /**
     * 批量插入（用于从备份恢复）：事务化先清空再插入，失败回滚保证零丢失
     */
    fun insertAll(wishes: List<WishItem>) {
        viewModelScope.launch {
            repository.replaceAll(wishes)
        }
    }

    /**
     * 标记心愿为已购买：将心愿转换为资产插入资产表，并从心愿表删除
     * @param wish 要购买的心愿
     * @param category 用户选择的资产分类
     * @param purchaseDate 购买日期（时间戳毫秒）
     * @param expectedYears 预期使用年限
     */
    fun purchaseWish(
        wish: WishItem,
        category: String,
        purchaseDate: Long,
        expectedYears: Int
    ) {
        viewModelScope.launch {
            // 1. 心愿转资产，插入资产表
            val asset = Asset(
                name = wish.name,
                purchasePrice = wish.price,
                purchaseDate = purchaseDate,
                expectedYears = expectedYears,
                category = category,
                status = "服役中",
                remark = wish.remark,
                imageUri = wish.imageUri.ifBlank { null }
            )
            assetRepository.insertAsset(asset)
            // 2. 从心愿表删除该心愿
            repository.deleteWish(wish)
        }
    }
}
