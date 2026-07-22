package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarromDao {
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("DELETE FROM matches")
    suspend fun clearMatchHistory()

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET progress = :progress, unlocked = :unlocked, unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun updateAchievementProgress(id: String, progress: Int, unlocked: Boolean, unlockedAt: Long?)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}
