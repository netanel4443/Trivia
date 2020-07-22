package com.e.trivia.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.e.trivia.R
import com.e.trivia.data.PlayerDetails
import com.e.trivia.ui.fragments.FragmentsTag
import com.e.trivia.ui.fragments.GameFragment
import com.e.trivia.utils.addFragment
import com.e.trivia.utils.livedata.toLiveData
import com.e.trivia.utils.livedata.toObservable
import com.e.trivia.viewmodels.MainScreenViewModel
import com.e.trivia.viewmodels.commands.MainScreenCommands
import com.e.trivia.viewmodels.commands.MainScreenCommandsEnum
import com.e.trivia.viewmodels.states.MainScreenStates
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseAdsActivity() {
    private val TAG="MainActivity"

    private val viewModel: MainScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        //temp
        playerDetails.text="Name: Netanel \nLevel: 0 \nScore: 0"

        attachStatesObserver()
        attachCommandsObserver()

        viewModel.getPlayerDetails()

        startGameBtn.setOnClickListener { viewModel.startGame() }

        /*this is only for personal user , Realm studio doesn't work properly so i need to
          create first template*/
        //    viewModel.createQuestionsRepo("fake",true)
    }

    private fun attachStatesObserver() {
        viewModel.state.observe(this, Observer{state->
            when(state){
                is MainScreenStates.StartGame -> startGame()
            }

        })
    }

    private fun attachCommandsObserver() {
        viewModel.commands.toLiveData().observe(this, Observer {state->
            val command=state.second
            when(state.first){
                 MainScreenCommandsEnum.ReadPlayerDetails -> updatePlayerDetailsDetails(command.readPlayerDetails)
            }
        })
    }

    private fun updatePlayerDetailsDetails(details:PlayerDetails) {
        playerDetails.text="Name:${details.name} \n Level: ${details.level}\n Score: ${details.score} \n ${details.coins}"
    }

    private fun startGame() {
        //todo should I inject the fragment?
        addFragment(GameFragment(),R.id.frame_layout_main_screen, FragmentsTag.GAME_FRAGMEN)
    }














}