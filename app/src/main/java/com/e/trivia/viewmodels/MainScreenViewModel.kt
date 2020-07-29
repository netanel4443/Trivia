package com.e.trivia.viewmodels

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.e.VoiceAssistant.utils.printErrorIfDebug
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.printInfoIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question
import com.e.trivia.domain.MainScreenUseCases
import com.e.trivia.utils.collection.take
import com.e.trivia.utils.livedata.SingleLiveEvent
import com.e.trivia.viewmodels.effects.MainScreenEffects
import com.e.trivia.viewmodels.states.MainScreenState
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class MainScreenViewModel() : BaseViewModel() {
    private val TAG="MainScreenViewModel"
    private val useCases=MainScreenUseCases()

    private val _timerState =MutableLiveData<Int>()
    val timerState:LiveData<Int> get()= _timerState

    private var _states= MainScreenState()
    val states = MutableLiveData(_states)

    private val _viewEffects=SingleLiveEvent<MainScreenEffects>()
    val viewEffects:LiveData<MainScreenEffects> get() = _viewEffects

    private var questionDisposable=CompositeDisposable()
    private val questionsFromDB= ArrayList<Question>()
    private var questions=ArrayList<Question>()
    private var remainingTime:Long=0
    private var takeTime:Long=60

    private fun updateState(_stateClone: MainScreenState){
        _states=_stateClone
        states.postValue(_states)
    }


    fun forceUpdateState(){
     val stateClone=_states.copy(forceRender = _states.forceRender+1)
         updateState(stateClone)
    }

    fun goToGameScreen(){
        _viewEffects.value=MainScreenEffects.StartGameScreen
    }

    fun startGameAllQuestions(){
        _states= MainScreenState(
            passPlayerDetails = _states.passPlayerDetails,
            forceRender = _states.forceRender
        )
        //reset timer
        questions=questionsFromDB
        questions.shuffle()
        remainingTime=0
        takeTime=60
        goToGameScreen()
    }

    fun showCustomGameDialog(){
       _viewEffects.value=MainScreenEffects.ShowCustomGameDialog(questionsFromDB.size)
    }

    fun startCustomGame(numberOfQuestions:Int?){
        if (numberOfQuestions==null || numberOfQuestions>questionsFromDB.size || numberOfQuestions<=0 ) {
            val comment= "choose a number from 1 to ${questionsFromDB.size}"
            _viewEffects.value=MainScreenEffects.CustomDialogGameCommentToUser(comment)
        }
        else{
            _states= MainScreenState(
                passPlayerDetails = _states.passPlayerDetails,
                forceRender = _states.forceRender
            )
            //reset timer
            remainingTime=0
            takeTime=60
            questionsFromDB.shuffle()
            questions=questionsFromDB.take(numberOfQuestions)
            _viewEffects.value=MainScreenEffects.DissmissCustomGameDialog
            goToGameScreen()
        }
    }

     fun firstGameInits(){
        if (questionsFromDB.isEmpty()) {
            val detailsOb = useCases.getPlayerDetails().toObservable()
                .doOnNext { updateState(_states.copy(passPlayerDetails = it)) }

            val questionsOb = useCases.getQuestions().toObservable()
                .doOnNext { questionsFromDB.addAll(it) }

            val obArray = arrayOf(detailsOb, questionsOb)

            +Observable.combineLatest(obArray) {}
                .subscribeOnIoAndObserveOnMain()
                .subscribe({}) { printIfDebug(TAG, it.message) }
        }
    }

    private fun getQuestionsFromDb(): Single<ArrayList<Question>> {
        return useCases.getQuestions()
            .doOnSuccess {
                questionsFromDB.addAll(it)
                printIfDebug(TAG,"$it")
            }
    }

    fun startQuestionTimer(prevRemainingTime:Long, take:Long){
        var progress:Long=0
        questionDisposable.clear() // clear previous timer Observable
        questionDisposable.add(useCases.timerInterval(take)
            .subscribeOnIoAndObserveOnMain()
            .doOnComplete {
                showGameOverDialogAndUpdateDatabase()
            }
            .subscribe({counter->
                remainingTime=prevRemainingTime+counter+1
                progress=60-remainingTime
                _timerState.value=progress.toInt()
            },{})
        )
    }

    fun pauseTimer(){
      questionDisposable.clear()
    }

    fun resumeTimer(){
      startQuestionTimer(remainingTime,takeTime)
    }

    fun getQuestion() {
       getQuestion(remainingTime,takeTime)
    }

   private fun getQuestion(remaining: Long, take: Long) {
        //todo when no more questions  handle game is finished
       questions.elementAtOrNull(_states.currentGameDetails.currentLevel)?.let { question->
            updateState(_states.copy(newQuestion=question))
            startQuestionTimer(remaining,take)
            enableAnswerBtns(true)
       }?:let {
          showGameOverDialogAndUpdateDatabase()
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
            getQuestion(0,60)// when new question , reset timer.
            val copy=_states.copy()
            copy.changeAnswerColor=Color.WHITE
            copy.changeAlpha=0f
            updateState(copy)
        }
    }

    private fun updatePlayerDetailsWhenGameOver():PlayerDetails{

        val tmpDetails=_states.passPlayerDetails.copy()
        if (_states.currentGameDetails.currentScore>_states.passPlayerDetails.highestScore)
            tmpDetails.highestScore = _states.currentGameDetails.currentScore
        if (_states.currentGameDetails.currentLevel>_states.passPlayerDetails.highestlevel)
            tmpDetails.highestlevel = _states.currentGameDetails.currentLevel
        updateState(_states.copy(passPlayerDetails = tmpDetails ))
        return tmpDetails
    }

    private fun increaseOrDecreaseScoreAndLevel(color:Int, additionalScore:Int) {
        var score=_states.currentGameDetails.currentScore
        if (color==Color.GREEN){
            changeScoreAnimation(color,additionalScore)//  animation limited to 1 sec
            score += additionalScore
        }

        val tmpCurrentGameDetails=MainScreenState.CurrentGameDetails(
             currentScore = score,
             currentLevel = _states.currentGameDetails.currentLevel+1
        )
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
            .subscribe({ block() }){ printErrorIfDebug(TAG,it.message) }
    }

    fun showGameOverDialogAndUpdateDatabase(){
        pauseTimer()// when game is over , stop timer observable.
        +useCases.saveOrUpdatePlayerDetails(updatePlayerDetailsWhenGameOver())
            .subscribeOnIoAndObserveOnMain()
            .subscribe({
                _viewEffects.value=(MainScreenEffects.ShowGameOverDialog(_states.passPlayerDetails,_states.currentGameDetails.currentScore))
            }){ printErrorIfDebug(TAG,it.message) }
    }

    fun updatePlayerDetailsDataBase(){
        +useCases.saveOrUpdatePlayerDetails(updatePlayerDetailsWhenGameOver())
            .subscribeOnIoAndObserveOnMain()
            .subscribe({}){ printErrorIfDebug(TAG,it.message)}
    }

    fun deletePlayerDetails(){
        +useCases.deletePlayerDetails(_states.passPlayerDetails.name)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({}){ printInfoIfDebug(TAG,it.message)}
    }

    /**for personal use because Realm studio(app) doesn't work properly*/
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