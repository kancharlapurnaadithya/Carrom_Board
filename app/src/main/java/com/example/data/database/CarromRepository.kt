package com.example.data.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CarromRepository(private val carromDao: CarromDao) {

    val allMatches: Flow<List<MatchEntity>> = carromDao.getAllMatches()
    val allAchievements: Flow<List<AchievementEntity>> = carromDao.getAllAchievements()

    suspend fun insertMatch(match: MatchEntity) {
        carromDao.insertMatch(match)
        // Auto-check achievement progress on inserting match
        checkMatchAchievements()
    }

    suspend fun clearMatchHistory() {
        carromDao.clearMatchHistory()
        // Reset achievement progress as well
        val current = carromDao.getAllAchievements().first()
        val reset = current.map {
            it.copy(progress = 0, unlocked = false, unlockedAt = null)
        }
        carromDao.insertAchievements(reset)
    }

    suspend fun updateAchievementProgress(id: String, progress: Int) {
        val list = carromDao.getAllAchievements().first()
        val achievement = list.find { it.id == id } ?: return
        if (achievement.unlocked) return

        val newProgress = progress.coerceAtMost(achievement.maxProgress)
        val isUnlocked = newProgress >= achievement.maxProgress
        val unlockTime = if (isUnlocked) System.currentTimeMillis() else null

        carromDao.updateAchievementProgress(id, newProgress, isUnlocked, unlockTime)
    }

    suspend fun initializeDefaultAchievements() {
        val existing = carromDao.getAllAchievements().first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                AchievementEntity(
                    id = "first_victory",
                    title = "First Victory",
                    description = "Win your first offline Carrom duel.",
                    progress = 0,
                    maxProgress = 1,
                    unlocked = false
                ),
                AchievementEntity(
                    id = "carrom_pro",
                    title = "Carrom Champion",
                    description = "Win 5 Carrom matches across any board theme.",
                    progress = 0,
                    maxProgress = 5,
                    unlocked = false
                ),
                AchievementEntity(
                    id = "queen_collector",
                    title = "Queen Collector",
                    description = "Pocket the Red Queen 5 times in matches.",
                    progress = 0,
                    maxProgress = 5,
                    unlocked = false
                ),
                AchievementEntity(
                    id = "unbeatable",
                    title = "Grandmaster",
                    description = "Win a match with a score difference of 15 or more points.",
                    progress = 0,
                    maxProgress = 1,
                    unlocked = false
                ),
                AchievementEntity(
                    id = "board_hopper",
                    title = "Stylish Striker",
                    description = "Play matches on 3 different board designs.",
                    progress = 0,
                    maxProgress = 3,
                    unlocked = false
                )
            )
            carromDao.insertAchievements(defaults)
        }
    }

    private suspend fun checkMatchAchievements() {
        val matches = carromDao.getAllMatches().first()
        if (matches.isEmpty()) return

        // 1. First victory
        updateAchievementProgress("first_victory", 1)

        // 2. Carrom Champion (5 wins)
        // Since we play local multiplayer, player custom names exist. Let's look up how many matches are registered
        updateAchievementProgress("carrom_pro", matches.size)

        // 3. Unbeatable check
        for (m in matches) {
            val scores = m.scoresJoined.split(",").mapNotNull { it.toIntOrNull() }
            if (scores.size >= 2) {
                val diff = kotlin.math.abs(scores[0] - scores[1])
                if (diff >= 15) {
                    updateAchievementProgress("unbeatable", 1)
                }
            }
        }
    }
}
