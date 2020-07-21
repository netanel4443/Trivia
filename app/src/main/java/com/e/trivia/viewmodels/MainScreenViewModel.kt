package com.e.trivia.viewmodels

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question
import com.e.trivia.domain.MainScreenUseCases
import com.e.trivia.viewmodels.commands.MainScreenCommands
import com.e.trivia.viewmodels.states.MainScreenStates
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainScreenViewModel(private val savedStateHandle:SavedStateHandle) : BaseViewModel() {
    private val TAG="MainScreenViewModel"
    private val useCases=MainScreenUseCases()
    private val _state =MutableLiveData<MainScreenStates>()
    val state:LiveData<MainScreenStates> get()= _state
    val commands = PublishSubject.create<MainScreenCommands>()

    private var questionDisposable=CompositeDisposable()
    private var playerDetails=PlayerDetails()
    private val questions= ArrayList<Question>()
    private var remainingTime:Long=60

    fun startGame(){
        _state.value=MainScreenStates.StartGame
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
//        savedStateHandle.get<PlayerDetails>("playerDetails")?.run { playerDetails=this }
        savedStateHandle.get<Long>("remainingTime")?.run { remainingTime=this }
        savedStateHandle.get<Int>("level")?.run{ playerDetails.level=this }
        savedStateHandle.get<Int>("score")?.run{ playerDetails.score=this }
        updateQuestion(playerDetails.level,remainingTime,60-remainingTime)
        commands.onNext(MainScreenCommands.PassPlayerDetails(playerDetails))
    }

    private fun firstGameInits(){

       val detailsOb =useCases.getPlayerDetails().toObservable()
           .doOnNext {
               playerDetails=it
               commands.onNext(MainScreenCommands.PassPlayerDetails(it))
           }
       val questionsOb= useCases.getQuestions().toObservable()
           .doOnNext {
               questions.addAll(it)
           }

       val obArray= arrayOf(detailsOb, questionsOb)

        +Observable.combineLatest(obArray){}
            .subscribeOnIoAndObserveOnMain()
            .subscribe({updateQuestion(playerDetails.level,0,60)}){ printIfDebug(TAG,it.message)}

    }

    private fun getQuestionsFromDb(): Single<ArrayList<Question>> {
        return useCases.getQuestions()
            .doOnSuccess {
                questions.addAll(it)
                printIfDebug(TAG,"$it")
            }
    }

    fun startQuestionTimer(initialDelay:Long,take:Long){
        var progress:Long=0
        questionDisposable.clear() // clear previous timer Observable
        questionDisposable.add(useCases.timerInterval(initialDelay,take)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({counter->
                printIfDebug("counter","$counter")
                remainingTime=initialDelay+counter+1
                progress=(60-remainingTime)
                _state.value=MainScreenStates.Progress(progress.toInt())
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
                        //set playerDetails
                        playerDetails = details
                        commands.onNext(MainScreenCommands.ReadPlayerDetails(details))
                    },
                    { printIfDebug(TAG, it.message) })
        }
        else{

        }
    }

    fun updateQuestion(level:Int,initialDelay: Long,take: Long) {
        //todo handle index out of bounds
        val question=questions.elementAtOrNull(level)?.let {it}?: Question("no more questions",true)
        commands.onNext(MainScreenCommands.NewQuestion(question))
        startQuestionTimer(initialDelay,take)
    }

    fun enableAnswerBtns(enable:Boolean) {
        commands.onNext(MainScreenCommands.EnableAnswerBtns(enable))
    }

    fun checkAnswer(answer:Boolean,level:Int) {
        val color = if (answer==questions.elementAtOrNull(level)?.answer) { Color.GREEN } else {Color.RED}
        commands.onNext(MainScreenCommands.ChangeAnswerColor(color))
        increaseOrDecreaseScore(color,50)// increase or decrease with  animation limited to 1 sec
        setTimer(1,TimeUnit.SECONDS){
            updateQuestion(level+1,0,60)
            updatePlayerDetails(50)
            commands.onNext(MainScreenCommands.ChangeAnswerColor(Color.WHITE))
            commands.onNext(MainScreenCommands.EnableAnswerBtns(true))
            commands.onNext(MainScreenCommands.ChangeVisibility(0f))
        }
    }

    private fun updatePlayerDetails(score:Int){
        playerDetails.score+=score
        playerDetails.level+=1
        commands.onNext(MainScreenCommands.PassPlayerDetails(playerDetails))
    }

    private fun increaseOrDecreaseScore(color:Int,score:Int) {
        if (color==Color.GREEN){
            changeScoreAnimation(color,score)//  animation limited to 1 sec
        }
    }

    private fun changeScoreAnimation(color: Int,score: Int) {
        commands.onNext(MainScreenCommands.ChangeScoreAnimation(color,score))
        commands.onNext(MainScreenCommands.ChangeVisibility(1f))
    }

    fun setTimer(time:Long,timeUnit: TimeUnit,block:()->Unit){
        +useCases.startTimer(time,timeUnit)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ block() }){ printIfDebug(TAG,it.message) }
    }

    fun saveData(){
        +useCases.updadatePlayerDetails(playerDetails)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({},{ printIfDebug(TAG,it.message)})
//        savedStateHandle.set("playerDetails", playerDetails)
        savedStateHandle.set("level",playerDetails.level)
        savedStateHandle.set("score",playerDetails.score)
        savedStateHandle.set("remainingTime", remainingTime)
        printIfDebug(TAG,"level ${playerDetails.level} ${playerDetails.score} ${remainingTime}")
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



