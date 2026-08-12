package com.example.prepx.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.prepx.data.model.PlannerItem

/**
 * Main Room Database singleton instance for PrepX application.
 * Manages planner_items table with diagnostic logging for debug tracking.
 */
@Database(entities = [PlannerItem::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plannerDao(): PlannerDao

    companion object {
        private const val TAG = "PrepX_Debug"
        private const val DATABASE_NAME = "prepx_database.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Initializing AppDatabase instance...")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                Log.d(TAG, "AppDatabase created successfully: $DATABASE_NAME")
                instance
            }
        }
    }
}
