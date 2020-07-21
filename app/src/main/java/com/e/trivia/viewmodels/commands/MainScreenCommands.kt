package com.e.trivia.viewmodels.commands

import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question

sealed class MainScreenCommands {
    data class ReadPlayerDetails(val details:PlayerDetails) :MainScreenCommands()
    data class PassPlayerDetails(val details:PlayerDetails) : MainScreenCommands()
    data class NewQuestion(val question: Question) : MainScreenCommands()
    data class EnableAnswerBtns(val enable: Boolean) : MainScreenCommands()
    data class ChangeAnswerColor(val color: Int) : MainScreenCommands()
    data class ChangeScoreAnimation(val color: Int,val score: Int) : MainScreenCommands()
    data class ChangeVisibility(val visibility: Float) : MainScreenCommands()
    data class UpdateOrSetTimer(val timeInterval:Long,val take:Long) : MainScreenCommands()
}