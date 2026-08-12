package com.example.zhangwu.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(
    entities = [AssetEntity::class, WishEntity::class, CategoryEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun wishDao(): WishDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 版本 2 → 3 迁移：新增 wishes 表用于持久化心愿单
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wishes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        targetDate TEXT NOT NULL,
                        isPurchased INTEGER NOT NULL DEFAULT 0,
                        icon TEXT NOT NULL DEFAULT '❤️',
                        remark TEXT NOT NULL DEFAULT '',
                        imageUri TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * 版本 3 → 4 迁移：空操作
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // no-op
            }
        }

        /**
         * 版本 4 → 5 迁移：新增 categories 表，并从 assets 表回填历史分类
         * 兼容性：使用 COUNT 子查询替代 ROW_NUMBER() 窗口函数（SQLite < 3.25 不支持窗口函数）
         * 并排除"全部"这类默认分类防止重复
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                // 从 assets 表提取已有分类，回填到 categories 表
                // 使用 COUNT 子查询生成排序号，兼容旧版本 SQLite；并排除默认分类"全部"
                db.execSQL(
                    """
                    INSERT INTO categories (name, sortOrder)
                    SELECT DISTINCT a1.category,
                           (SELECT COUNT(DISTINCT a2.category)
                            FROM assets a2
                            WHERE a2.category < a1.category
                              AND a2.category != '' AND a2.category IS NOT NULL
                              AND a2.category != '全部')
                    FROM assets a1
                    WHERE a1.category != '' AND a1.category IS NOT NULL
                      AND a1.category != '全部'
                    """.trimIndent()
                )
            }
        }

        /**
         * 版本 5 → 6 迁移：清理 categories 表中的默认分类脏数据（如"全部"）
         * 防止 ViewModel 拼接时出现重复 key 导致闪退
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM categories WHERE name = '全部'")
            }
        }

        /**
         * 版本 6 → 7 迁移：对 categories 表进行全面去重清洗
         * 旧版 MIGRATION_4_5 使用 ROW_NUMBER() 导致 SELECT DISTINCT 失效，产生大量重复记录（工具/数码等）
         * 按 name 分组保留 id 最小的一条，删除所有重复行，彻底解决 key 重复闪退问题
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    DELETE FROM categories
                    WHERE id NOT IN (
                        SELECT MIN(id) FROM categories GROUP BY name
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zhangwu_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
