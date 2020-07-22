package com.e.trivia.viewmodels.commands

import android.graphics.Color
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question

data class MainScreenCommands(
    var readPlayerDetails:PlayerDetails=PlayerDetails(),
    var passPlayerDetails:PlayerDetails= PlayerDetails(),
    var newQuestion:Question= Question("",false),
    var enableAnswerBtns:Boolean= true,
    var changeAnswerColor:Int= Color.BLACK,
    var changeScoreAnimation:ChangeScoreAnimation= ChangeScoreAnimation(Color.GREEN,0),
    var changeAlpha:Float=0f,
    var updateOrSetTimer: UpdateOrSetTimer=UpdateOrSetTimer(60,60)
) {
    data class ChangeScoreAnimation(val color: Int,val score: Int)
    data class UpdateOrSetTimer(val timeInterval:Long,val take:Long)

}

enum class MainScreenCommandsEnum{
    ReadPlayerDetails,
    PassPlayerDetails,
    NewQuestion,
    EnableAnswerBtns,
    ChangeAnswerColor,
    ChangeScoreAnimation,
    ChangeAlpha,
    UpdateOrSetTimer,
}