package com.example.zhangwu.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT name FROM categories ORDER BY sortOrder ASC")
    suspend fun getAllCategoryNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    // 按名称删除分类（避免@Delete依赖主键匹配的问题）
    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun deleteCategoryByName(name: String)

    // 按名称更新排序号（避免@Update依赖主键匹配的问题）
    @Query("UPDATE categories SET sortOrder = :sortOrder WHERE name = :name")
    suspend fun updateSortOrderByName(name: String, sortOrder: Int)

    @Query("UPDATE categories SET name = :newName WHERE name = :oldName")
    suspend fun renameCategory(oldName: String, newName: String)

    @Query("UPDATE assets SET category = :newName WHERE category = :oldName")
    suspend fun updateAssetCategory(oldName: String, newName: String)

    @Query("SELECT DISTINCT category FROM assets WHERE category != ''")
    suspend fun getDistinctAssetCategories(): List<String>
}
