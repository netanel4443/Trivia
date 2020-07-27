package com.e.trivia.viewmodels

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.printInfoIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question
import com.e.trivia.domain.MainScreenUseCases
import com.e.trivia.utils.livedata.SingleLiveEvent
import com.e.trivia.viewmodels.states.MainScreenState
import com.e.trivia.viewmodels.effects.MainScreenEffects
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class MainScreenViewModel(private val savedStateHandle:SavedStateHandle) : BaseViewModel() {
    private val TAG="MainScreenViewModel"
    private val useCases=MainScreenUseCases()

    private val _timerState =MutableLiveData<Int>()
    val timerState:LiveData<Int> get()= _timerState

    private var _states= MainScreenState()
    val states = MutableLiveData(_states)

    private val _viewEffects=SingleLiveEvent<MainScreenEffects>()
    val viewEffects:LiveData<MainScreenEffects> get() = _viewEffects

    private var questionDisposable=CompositeDisposable()
    private val questions= ArrayList<Question>()
    private var remainingTime:Long=0
    private var takeTime:Long=60

    private fun updateState(_stateClone: MainScreenState){
        _stateClone.isConfiguration=false
        _states=_stateClone
        states.postValue(_states)
    }

    private fun forceUpdateState(stateClone: MainScreenState){
        stateClone.isConfiguration = true
        states.postValue(stateClone)
    }

    fun forceUpdateState(){
        forceUpdateState(_states)
    }


    fun goToGameScreen(){
        _viewEffects.value=MainScreenEffects.StartGameScreen
    }

    fun startGameAllQuestions(){
        _states= MainScreenState(
            readPlayerDetails = _states.readPlayerDetails,
            passPlayerDetails = _states.passPlayerDetails
        )
        forceUpdateState(_states)
    }

     fun firstGameInits(){
        if (questions.isEmpty()) {
            val detailsOb = useCases.getPlayerDetails().toObservable()
                .doOnNext {
                    updateState(_states.copy(passPlayerDetails = it))
                }
            val questionsOb = useCases.getQuestions().toObservable()
                .doOnNext {
                    questions.addAll(it)
                }

            val obArray = arrayOf(detailsOb, questionsOb)

            +Observable.combineLatest(obArray) {}
                .subscribeOnIoAndObserveOnMain()
                .subscribe({}) { printIfDebug(TAG, it.message) }
        }
    }

    private fun getQuestionsFromDb(): Single<ArrayList<Question>> {
        return useCases.getQuestions()
            .doOnSuccess {
                questions.addAll(it)
                printIfDebug(TAG,"$it")
            }
    }

    fun startQuestionTimer(prevRemainingTime:Long, take:Long){
        var progress:Long=0
        questionDisposable.clear() // clear previous timer Observable
        questionDisposable.add(useCases.timerInterval(take)
            .subscribeOnIoAndObserveOnMain()
            .doOnComplete {
                printInfoIfDebug(TAG,"completed")
                updatePlayerDetailsWhenGameOver()
                _viewEffects.value=(MainScreenEffects.ShowGameOverDialog(_states.passPlayerDetails,_states.currentGameDetails.currentScore))
            }
            .subscribe({counter->
                remainingTime=prevRemainingTime+counter+1
                progress=60-remainingTime
                _timerState.value=progress.toInt()
            },{})
        )
    }

     fun getQuestion() {
           getQuestion(remainingTime,takeTime)
    }

   private fun getQuestion(initialDelay: Long, take: Long) {
        //todo no more questions logic and game over
        questions.elementAtOrNull(_states.currentGameDetails.currentLevel)?.let {question->
            updateState(_states.copy(newQuestion=question))
            startQuestionTimer(initialDelay,take)
        }?:let {
           val question= Question("no more questions", true)
        updateState( _states.copy(newQuestion = question))
        }

   }

   fun enableAnswerBtns(enable:Boolean) {
       updateState(_states.copy(enableAnswerBtns=enable))
   }

    fun checkAnswer(answer:Boolean) {
        val color = if (answer==questions.elementAtOrNull(_states.currentGameDetails.currentLevel)?.answer) { Color.GREEN } else {Color.RED}
        updateState(_states.copy(changeAnswerColor = color))
        increaseOrDecreaseScoreAndLevel(color,50)// increase or decrease with  animation limited to 1 sec

        setDelay(1,TimeUnit.SECONDS){
            getQuestion(0,60)
            val copy=_states.copy()
            copy.changeAnswerColor=Color.WHITE
            copy.enableAnswerBtns=true
            copy.changeAlpha=0f
            updateState(copy)
        }
    }

    private fun updatePlayerDetailsWhenGameOver(){
    //todo change this 3 lines it shouldnt be here , it should be after game is finished or closed!!
        val tmpDetails=PlayerDetails()
        if (_states.currentGameDetails.currentScore>_states.passPlayerDetails.highestScore)
            tmpDetails.highestScore = _states.currentGameDetails.currentScore
        if (_states.currentGameDetails.currentLevel>_states.passPlayerDetails.highestlevel)
            tmpDetails.highestlevel = _states.currentGameDetails.currentLevel
        updateState(_states.copy(passPlayerDetails = tmpDetails ))
    }

    private fun increaseOrDecreaseScoreAndLevel(color:Int, score:Int) {
        if (color==Color.GREEN){
            changeScoreAnimation(color,score)//  animation limited to 1 sec
        }

        val tmpCurrentGameDetails=MainScreenState.CurrentGameDetails(
            _states.currentGameDetails.currentScore+score,
             _states.currentGameDetails.currentLevel+1)
        val stateCopy=_states.copy(currentGameDetails = tmpCurrentGameDetails)
        updateState(stateCopy)
    }

    private fun changeScoreAnimation(color: Int,score: Int) {
        updateState(_states.copy(changeScoreAnimation= MainScreenState.ChangeScoreAnimation(color,score)))
        updateState(_states.copy(changeAlpha = 1f))
    }

    private fun setDelay(time:Long, timeUnit: TimeUnit, block:()->Unit){
        +useCases.startTimer(time,timeUnit)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ block() }){ printIfDebug(TAG,it.message) }
    }

    //for personal use becuse Realm studio doesn't work properly
    fun createQuestionsRepo(question: String,answer:Boolean){
        +useCases.createDbOfQuestions(question,answer)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({}){ printIfDebug(TAG,it.message)}
    }

    override fun onCleared() {
        super.onCleared()
        questionDisposable.clear()
    }
}