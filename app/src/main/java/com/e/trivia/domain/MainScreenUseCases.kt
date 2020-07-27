package com.e.trivia.domain

import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.Question
import com.e.trivia.data.repo.QuestionsRepo
import com.e.trivia.data.repo.RepoCrud
import io.reactivex.Completable
import io.reactivex.Flowable.interval
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import java.util.concurrent.TimeUnit

class MainScreenUseCases() {
    private val repo = RepoCrud()
    private val questionsRepo=QuestionsRepo()

    fun getPlayerDetails(): Single<PlayerDetails> {
        return repo.readPlayerDetails()
    }

    fun startTimer(time:Long,timeUnit: TimeUnit):Completable{
       return Completable.timer(time,timeUnit)

    }

    fun timerInterval(take:Long): Observable<Long> {
        return Observable
                 .interval(1,TimeUnit.SECONDS)
                 .take(take)
    }

    // for personal use because Realm studio doesn't work properly
    fun createDbOfQuestions(question: String,answer:Boolean):Completable{
        return repo.createQuestionsDb(question,answer)
    }

    fun getQuestions():Single<ArrayList<Question>>{
        return questionsRepo.getQuestions()
    }

    fun deletePlayerDetails(playerDetails: String):Completable{
        return repo.deletePlayerDetails(playerDetails)
    }

     fun saveOrUpdatePlayerDetails(playerDetails: PlayerDetails): Completable {
        return repo.savePlayerDetails(playerDetails)
    }
}