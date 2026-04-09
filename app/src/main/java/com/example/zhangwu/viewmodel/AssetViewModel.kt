package com.example.zhangwu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhangwu.database.AppDatabase
import com.example.zhangwu.database.AssetRepository
import com.example.zhangwu.model.Asset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AssetRepository

    init {
        val assetDao = AppDatabase.getDatabase(application).assetDao()
        repository = AssetRepository(assetDao)
    }

    val allAssets: Flow<List<Asset>> = repository.getAllAssets()

    fun insertAsset(asset: Asset) = viewModelScope.launch {
        repository.insertAsset(asset)
    }

    fun updateAsset(asset: Asset) = viewModelScope.launch {
        repository.updateAsset(asset)
    }

    fun deleteAsset(asset: Asset) = viewModelScope.launch {
        repository.deleteAsset(asset)
    }
}