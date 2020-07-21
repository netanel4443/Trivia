package com.e.trivia.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.trivia.R
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question
import com.e.trivia.utils.livedata.toObservable
import com.e.trivia.viewmodels.MainScreenViewModel
import com.e.trivia.viewmodels.commands.MainScreenCommands
import com.e.trivia.viewmodels.states.MainScreenStates
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_game.*


class GameFragment : BaseFragment() {
    private val TAG="GameFragment"
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

      +viewModel.commands.toObservable(viewLifecycleOwner).distinctUntilChanged()
          .scan{prev,now->renderState(prev,now)}
          .subscribe({}){ printIfDebug(TAG,it.message)}
    }

    private fun renderState(prev:MainScreenCommands,now:MainScreenCommands):MainScreenCommands{
        println("a!=b ${prev!=now}")

        if (prev.passPlayerDetails!=now.passPlayerDetails) { passedPlayerDetailsFromActivity(now.passPlayerDetails)}
        if (prev.newQuestion!=now.newQuestion){ updateQuestion(now.newQuestion)}
        if (prev.enableAnswerBtns!=now.enableAnswerBtns){ enableAnswerButtons(now.enableAnswerBtns)}
        if (prev.changeAnswerColor!=now.changeAnswerColor){ isAnswerCorrectColor(now.changeAnswerColor)}
        if (prev.changeScoreAnimation!=now.changeScoreAnimation){changeScoreAnimation(now.changeScoreAnimation.color,now.changeScoreAnimation.score)}
        if (prev.changeAlpha!=now.changeAlpha){changeScoreTviewVisibility(now.changeAlpha)}
        if (prev.updateOrSetTimer!=now.updateOrSetTimer){updateTime(now.updateOrSetTimer.timeInterval,now.updateOrSetTimer.take)}

        return now
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