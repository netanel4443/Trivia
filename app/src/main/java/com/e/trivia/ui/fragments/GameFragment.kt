package com.e.trivia.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.toPublisher
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.trivia.R
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question
import com.e.trivia.utils.livedata.toLiveData
import com.e.trivia.utils.livedata.toObservable
import com.e.trivia.viewmodels.MainScreenViewModel
import com.e.trivia.viewmodels.commands.MainScreenCommands
import com.e.trivia.viewmodels.commands.MainScreenCommandsEnum
import com.e.trivia.viewmodels.states.MainScreenStates
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_game.*


class GameFragment : BaseFragment() {

    private  val viewModel: MainScreenViewModel by activityViewModels()
    private  var playerDetails=PlayerDetails()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.fragment_game, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachStatesObserver()
        attachCommandsObserver()

        firstInits()

        +yesAnswerBtnGameFragment.clicks().throttle().subscribe{
            viewModel.checkAnswer(true,playerDetails.level)
            viewModel.enableAnswerBtns(false)// don't forget to enable back
        }

        +noAnswerBtnGameFragment.clicks().throttle().subscribe{
            viewModel.checkAnswer(false,playerDetails.level)
            viewModel.enableAnswerBtns(false) // don't forget to enable back
        }

    }

    private fun firstInits() {
        viewModel.firstInitOrResotre()
    }

    private fun attachStatesObserver() {
        viewModel.state.observe(viewLifecycleOwner, Observer{state->
            when(state){
                is MainScreenStates.Progress->setProgressBarProgress(state.progress)
            }
        })
    }

    private fun attachCommandsObserver() {

        +viewModel.commands.subscribe{  state->
            val command=state.second
            println("state.first ${state.first}")
            when(state.first){
                 MainScreenCommandsEnum.PassPlayerDetails -> passedPlayerDetailsFromActivity(command.passPlayerDetails)
                 MainScreenCommandsEnum.NewQuestion-> updateQuestion(command.newQuestion)
                 MainScreenCommandsEnum.EnableAnswerBtns -> { enableAnswerButtons(command.enableAnswerBtns) }
                 MainScreenCommandsEnum.ChangeAnswerColor -> isAnswerCorrectColor(command.changeAnswerColor)
                 MainScreenCommandsEnum.ChangeScoreAnimation -> changeScoreAnimation(command.changeScoreAnimation.color,command.changeScoreAnimation.score)
                 MainScreenCommandsEnum.ChangeAlpha -> changeScoreTviewVisibility(command.changeAlpha)
                 MainScreenCommandsEnum.UpdateOrSetTimer-> updateTime(command.updateOrSetTimer.timeInterval,command.updateOrSetTimer.take)
            }
        }
    }

    private fun changeScoreTviewVisibility(visibility: Float) {
        addedScoreTviewGameFragment.alpha=visibility
    }

    private fun changeScoreAnimation(color: Int, score: Int) {
        val fadeAndZoomIn=AnimationUtils.loadAnimation(requireContext(),R.anim.fade_and_zoom_in)
        addedScoreTviewGameFragment.setTextColor(color)
        addedScoreTviewGameFragment.text="+$score"
        addedScoreTviewGameFragment.animation=fadeAndZoomIn
    }

    private fun isAnswerCorrectColor(color: Int) {
        questionGameFragment.setTextColor(color)
    }

    private fun setProgressBarProgress(progress: Int){
        timerProgressBarGameFragment.progress=progress
    }

    private fun updateQuestion(question: Question) {
        questionGameFragment.text=question.question
        //       viewModel.startQuestionTimer(0,60)//start from 0 and repeat 60 times
    }

    private fun updateTime(initialDelay:Long,take:Long){
        viewModel.startQuestionTimer(initialDelay,take)//start from 0 and repeat 60 times
    }

    private fun passedPlayerDetailsFromActivity(details: PlayerDetails) {
        playerDetails=details
        scoreGameFragmentsTview.text="Score: "+details.score.toString()
        coinsGameFragmentsTview.text="Coins: "+details.coins.toString()
    }

    private fun enableAnswerButtons(enabled:Boolean){
        yesAnswerBtnGameFragment.isEnabled=enabled
        noAnswerBtnGameFragment.isEnabled=enabled
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveData()
    }
}