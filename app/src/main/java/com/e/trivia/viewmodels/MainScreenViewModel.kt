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
    val states = MutableLiveData<MainScreenState>()

    private val _viewEffects=SingleLiveEvent<MainScreenEffects>()
    val viewEffects:LiveData<MainScreenEffects> get() = _viewEffects

    private var questionDisposable=CompositeDisposable()
    private val questions= ArrayList<Question>()
    private var remainingTime:Long=60

    private fun updateState(_stateClone: MainScreenState){
        _stateClone.isConfiguration=false
        _states=_stateClone
        states.postValue(_states)
    }

    private fun updateStateAfterConfiguration(stateClone: MainScreenState){
        stateClone.isConfiguration = true
        states.postValue(stateClone)
    }

    fun startGame(){
        _viewEffects.value=MainScreenEffects.StartGameScreen
    }

    fun startGameAllQuestions(){
        _states= MainScreenState(
            readPlayerDetails = _states.readPlayerDetails,
            passPlayerDetails = _states.passPlayerDetails
        )
    }

    fun firstInitOrResotre(){
        if (savedStateHandle.keys().isEmpty()){
            firstGameInits()
        }
        else{
           restoreData()
        }
    }

    private fun restoreData(){
        if (questions.isEmpty()) {
            +getQuestionsFromDb()
                .subscribeOnIoAndObserveOnMain()
                .subscribe({
                    updateUiWithRestoredData()
                }) { printIfDebug(TAG, it.message) }
        }
        else{
            updateUiWithRestoredData()
        }

    }

    private fun updateUiWithRestoredData(){

        val playerDetails=PlayerDetails()
//        savedStateHandle.get<PlayerDetails>("playerDetails")?.run { playerDetails=this }
        savedStateHandle.get<Long>("remainingTime")?.run { remainingTime=this }
        savedStateHandle.get<Int>("level")?.run{ playerDetails.Highestlevel=this }
        savedStateHandle.get<Int>("score")?.run{ playerDetails.highestScore=this }
        updateQuestion(playerDetails.Highestlevel,remainingTime,60-remainingTime)

        updateStateAfterConfiguration( _states.copy(passPlayerDetails = playerDetails))

    }

    private fun firstGameInits(){

        val detailsOb =useCases.getPlayerDetails().toObservable()
            .doOnNext {
                updateState(_states.copy(passPlayerDetails =it))
            }
        val questionsOb= useCases.getQuestions().toObservable()
            .doOnNext {
                questions.addAll(it)//todo to keep in states?
            }

        val obArray= arrayOf(detailsOb, questionsOb)

        +Observable.combineLatest(obArray){}
            .subscribeOnIoAndObserveOnMain()
            .subscribe({updateQuestion(_states.passPlayerDetails.Highestlevel,0,60)}){ printIfDebug(TAG,it.message)}

    }

    private fun getQuestionsFromDb(): Single<ArrayList<Question>> {
        return useCases.getQuestions()
            .doOnSuccess {
                questions.addAll(it)//todo to keep in states?
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
                _viewEffects.value=(MainScreenEffects.ShowGameOverDialog(_states.passPlayerDetails,_states.currentScore))
            }
            .subscribe({counter->
                remainingTime=prevRemainingTime+counter+1
                progress=60-remainingTime
                _timerState.value=progress.toInt()
            },{})
        )
    }

    fun getPlayerDetails() {
        //todo fix this.Its been called from mainActivity
        if (savedStateHandle.keys().isEmpty()) {
            +useCases.getPlayerDetails()
                .subscribeOnIoAndObserveOnMain()
                .subscribe(
                    { details ->
                        val copy=_states.copy(readPlayerDetails= details)
                        updateState(copy)
                    },
                    { printIfDebug(TAG, it.message) })
        }
        else{

        }
    }

    fun updateQuestion(level:Int,initialDelay: Long,take: Long) {
        //todo handle index out of bounds
        val question=questions.elementAtOrNull(level)?.let {it}?: Question("no more questions",true)
        updateState(_states.copy(newQuestion=question))
        startQuestionTimer(initialDelay,take)
    }

    fun enableAnswerBtns(enable:Boolean) {
        updateState(_states.copy(enableAnswerBtns=enable))
    }

    fun checkAnswer(answer:Boolean) {
        val color = if (answer==questions.elementAtOrNull(_states.passPlayerDetails.Highestlevel)?.answer) { Color.GREEN } else {Color.RED}
        updateState(_states.copy(changeAnswerColor = color))
        increaseOrDecreaseScore(color,50)// increase or decrease with  animation limited to 1 sec

        setTimer(1,TimeUnit.SECONDS){
            updatePlayerDetails(50)//increase level +1 and then update question
            updateQuestion(_states.passPlayerDetails.Highestlevel,0,60)
            val copy=_states.copy()
            copy.changeAnswerColor=Color.WHITE
            copy.enableAnswerBtns=true
            copy.changeAlpha=0f
            updateState(copy)
        }
    }

    private fun updatePlayerDetails(score:Int){

        val tmpDetails=PlayerDetails( highestScore = _states.passPlayerDetails.highestScore+score,
                                      Highestlevel = _states.passPlayerDetails.Highestlevel+1)
        updateState(_states.copy(passPlayerDetails = tmpDetails ))
    }

    private fun increaseOrDecreaseScore(color:Int,score:Int) {
        if (color==Color.GREEN){
            changeScoreAnimation(color,score)//  animation limited to 1 sec
        }
    }

    private fun changeScoreAnimation(color: Int,score: Int) {
        updateState(_states.copy(changeScoreAnimation= MainScreenState.ChangeScoreAnimation(color,score)))
        updateState(_states.copy(changeAlpha = 1f))
    }

    fun setTimer(time:Long,timeUnit: TimeUnit,block:()->Unit){
        +useCases.startTimer(time,timeUnit)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ block() }){ printIfDebug(TAG,it.message) }
    }

    fun saveData(){
        val playerDetails=_states.passPlayerDetails
        +useCases.updadatePlayerDetails(playerDetails)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({},{ printIfDebug(TAG,it.message)})
//        savedStateHandle.set("playerDetails", playerDetails)
        savedStateHandle.set("level",playerDetails.Highestlevel)
        savedStateHandle.set("score",playerDetails.highestScore)
        savedStateHandle.set("remainingTime", remainingTime)
        printIfDebug(TAG,"level ${playerDetails.Highestlevel} ${playerDetails.highestScore} ${remainingTime}")
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