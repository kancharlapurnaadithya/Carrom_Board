package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MatchEntity::class, AchievementEntity::class], version = 1, exportSchema = false)
abstract class CarromDatabase : RoomDatabase() {
    abstract fun carromDao(): CarromDao

    companion object {
        @Volatile
        private var INSTANCE: CarromDatabase? = null

        fun getDatabase(context: Context): CarromDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CarromDatabase::class.java,
                    "carrom_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
