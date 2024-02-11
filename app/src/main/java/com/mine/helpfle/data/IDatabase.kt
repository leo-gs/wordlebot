package com.mine.helpfle.data

// Interface abstracting UI from database implementation
interface IDatabase {

    enum class OUTCOME { WON, LOST }

    fun getCurrentSolution() : String

    fun onFinishGame(gameState : OUTCOME)

    fun lookupGuess(guess : String) : Boolean
}