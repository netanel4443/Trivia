package com.e.trivia.viewmodels.effects

import com.e.trivia.data.PlayerDetails

sealed class MainScreenEffects {
    object StartGameScreen : MainScreenEffects()
    data class ShowGameOverDialog(val playerDetails: PlayerDetails,val gameScore:Int):MainScreenEffects()
    data class ShowCustomGameDialog(val maxNumberOfQuestions:Int) : MainScreenEffects()
    data class CustomDialogGameCommentToUser(val comment:String) : MainScreenEffects()
    object DissmissCustomGameDialog : MainScreenEffects()
}