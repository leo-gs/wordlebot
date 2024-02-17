package com.mine.helpfle.data

import java.io.Serializable

data class UserStats (
    val numGames: Int,
    val gamesWon: Int,
    val gamesLost: Int,
    val avgGuesses: Double,
    val currentStreak: Int,
    val longestStreak: Int
) : Serializable
