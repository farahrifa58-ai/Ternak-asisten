package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FarmDao
import com.example.data.entity.FarmProfile
import com.example.data.entity.FinanceTransaction
import com.example.data.entity.Harvest
import com.example.data.entity.InventoryItem
import com.example.data.entity.InventoryLog
import com.example.data.entity.Livestock
import com.example.data.entity.PopulationLog
import com.example.data.entity.Sale

@Database(
    entities = [
        FarmProfile::class,
        Livestock::class,
        PopulationLog::class,
        Harvest::class,
        Sale::class,
        InventoryItem::class,
        InventoryLog::class,
        FinanceTransaction::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ternak_asisten_pro_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
