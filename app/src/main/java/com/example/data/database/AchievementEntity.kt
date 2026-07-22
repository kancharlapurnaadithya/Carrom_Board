package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val maxProgress: Int,
    val unlocked: Boolean,
    val unlockedAt: Long? = null
)
