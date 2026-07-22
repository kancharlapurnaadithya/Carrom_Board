package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playersJoined: String, // Comma-separated names, e.g., "Player 1,Player 2"
    val scoresJoined: String,  // Comma-separated scores, e.g., "25,12"
    val winnerName: String,    // Name of the winner
    val gameDurationSec: Int,  // Duration of the game in seconds
    val timestamp: Long = System.currentTimeMillis()
)
