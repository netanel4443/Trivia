package com.e.trivia.viewmodels.states

import android.graphics.Color
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question

data class MainScreenState(
    var passPlayerDetails:PlayerDetails= PlayerDetails(),
    var newQuestion:Question= Question("",false),
    var enableAnswerBtns:Boolean= true,
    var changeAnswerColor:Int= Color.WHITE,
    var changeScoreAnimation: ChangeScoreAnimation = ChangeScoreAnimation(Color.GREEN, 0),
    var changeAlpha:Float=0f,
    var updateOrSetTimer: UpdateOrSetTimer = UpdateOrSetTimer(60, 60),
    var currentGameDetails: CurrentGameDetails= CurrentGameDetails(0,0),
    var forceRender:Int=0,
    var remainingTimer:Long=0,
    var takeTime:Long=60

) {
    data class ChangeScoreAnimation(val color: Int,val score: Int)
    data class UpdateOrSetTimer(val timeInterval:Long,val take:Long)
    data class CurrentGameDetails(val currentScore:Int,val currentLevel:Int)
}
