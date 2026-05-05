package com.kaufee.bv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaufee.bv.data.local.dao.HistoryDao
import com.kaufee.bv.data.local.entity.HistoryEntity

@Database(entities = [HistoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
