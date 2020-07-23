package com.e.trivia.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.trivia.R
import com.e.trivia.data.PlayerDetails
import com.e.trivia.ui.fragments.FragmentsTag
import com.e.trivia.ui.fragments.GameFragment
import com.e.trivia.utils.addFragment
import com.e.trivia.utils.livedata.toObservable
import com.e.trivia.viewmodels.MainScreenViewModel
import com.e.trivia.viewmodels.states.MainScreenState
import com.e.trivia.viewmodels.effects.MainScreenEffects
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseAdsActivity() {
    private val TAG="MainActivity"

    private val viewModel: MainScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        attachEffectsObserver()
        attachCommandsObserver()

        viewModel.getPlayerDetails()

        startGameBtn.setOnClickListener { viewModel.startGame() }

        /**this is only for personal user , Realm studio doesn't work properly so i need to
           create first template*/
        //    viewModel.createQuestionsRepo("fake",true)
    }

    private fun attachEffectsObserver() {
        viewModel.viewEffects.observe(this, Observer{ effect->
            when(effect){
                is MainScreenEffects.StartGameScreen -> startGame()
            }
        })
    }

    private fun attachCommandsObserver() {
        +viewModel.states.toObservable(this)
            .scan{prev,now->renderState(prev,now)}
            .subscribe({}){ printIfDebug(TAG,it.message) }
    }

    private fun renderState(prev: MainScreenState, now: MainScreenState): MainScreenState {
        if (prev.readPlayerDetails!=now.readPlayerDetails){updatePlayerDetailsDetails(now.readPlayerDetails)}
        return now
    }

    private fun updatePlayerDetailsDetails(details:PlayerDetails) {
        playerDetails.text="Name:${details.name} \n Level: ${details.Highestlevel}\n Score: ${details.highestScore} \n ${details.diamonds}"
    }

    private fun startGame() {
        addFragment(GameFragment(),R.id.frame_layout_main_screen, FragmentsTag.GAME_FRAGMEN)
    }
}