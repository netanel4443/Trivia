package com.e.trivia.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.e.VoiceAssistant.utils.printInfoIfDebug
import com.e.VoiceAssistant.utils.toast
import com.e.trivia.R
import com.e.trivia.data.PlayerDetails
import com.e.trivia.ui.dialogs.CustomGameDialog
import com.e.trivia.ui.fragments.FragmentsTag
import com.e.trivia.ui.fragments.GameFragment
import com.e.trivia.utils.addFragment
import com.e.trivia.utils.handleBackPressWhenFragmentAttached
import com.e.trivia.utils.livedata.toObservable
import com.e.trivia.viewmodels.MainScreenViewModel
import com.e.trivia.viewmodels.states.MainScreenState
import com.e.trivia.viewmodels.effects.MainScreenEffects
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseAdsActivity() {

    private val TAG="MainActivity"
    private val customGameDialog:CustomGameDialog by lazy{ CustomGameDialog(this) }
    private val viewModel: MainScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        attachEffectsObserver()
        attachStatesObserver()

        viewModel.firstGameInits()
        viewModel.forceUpdateState()

        startAllQuestionsGameBtn.setOnClickListener {
            viewModel.startGameAllQuestions()
        }
        custom_game_main_screen.setOnClickListener {
            viewModel.showCustomGameDialog()
        }

        /**this is only for personal user , Realm studio doesn't work properly so i need to
           create first template*/
        //    viewModel.createQuestionsRepo("fake",true)
    }

    private fun attachEffectsObserver() {
        viewModel.viewEffects.observe(this, Observer{ effect->
            when(effect){
                is MainScreenEffects.StartGameScreen -> startGame()
                is MainScreenEffects.ShowCustomGameDialog ->showCustomGameDialog(effect.maxNumberOfQuestions)
                is MainScreenEffects.CustomDialogGameCommentToUser-> customDialogGameCommentToUser(effect.comment)
                is MainScreenEffects.DissmissCustomGameDialog-> dissmissCustomGameDialog()
            }
        })
    }



    private fun attachStatesObserver() {
        +viewModel.states.toObservable(this)
            .scan{prev,now->renderState(prev,now)}
            .subscribe({}){ printInfoIfDebug(TAG,it.message) }
    }

    private fun renderState(prev: MainScreenState, now: MainScreenState): MainScreenState {
//        println("${prev} \n $now")
        val forceRender=now.forceRender>prev.forceRender

        if (prev.passPlayerDetails!=now.passPlayerDetails || forceRender){updatePlayerDetailsDetails(now.passPlayerDetails)}
        return now
    }

    private fun updatePlayerDetailsDetails(details:PlayerDetails) {
        playerDetails.text="Name:${details.name} \n Level: ${details.highestlevel}\n Score: ${details.highestScore} \n ${details.diamonds}"
    }

    private fun startGame() {
        addFragment(GameFragment(),R.id.frame_layout_main_screen, FragmentsTag.GAME_FRAGMEN)
    }

    private fun showCustomGameDialog(maxNumberOfQuestions:Int){
        customGameDialog.show(this,maxNumberOfQuestions.toString()){
            viewModel.startCustomGame(it)
        }
    }

    private fun customDialogGameCommentToUser(comment:String){
        toast(comment)
    }

    private fun dissmissCustomGameDialog() {
        customGameDialog.dismiss()
    }

    override fun onBackPressed() {
       handleBackPressWhenFragmentAttached()?.let{fragmentName->
           if (fragmentName==FragmentsTag.GAME_FRAGMEN){
               viewModel.showGameOverDialogAndUpdateDatabase()
           }
       }?:(
        super.onBackPressed()
        )
    }
}