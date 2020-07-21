package com.e.trivia.viewmodels.states

sealed class MainScreenStates {

    data class Progress(val progress:Int) : MainScreenStates()
    object StartGame : MainScreenStates()
}