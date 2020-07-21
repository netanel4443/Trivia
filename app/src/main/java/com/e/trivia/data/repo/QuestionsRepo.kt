package com.e.trivia.data.repo

import com.e.trivia.data.Question
import com.e.trivia.data.repo.configurations.QuestionConfiguration
import com.e.trivia.realmdbobjects.QuestionRealmObject
import io.reactivex.Single
import io.realm.Realm
import java.util.*
import kotlin.collections.LinkedHashSet

class QuestionsRepo {

    fun getQuestions():Single<ArrayList<Question>>{

        return Single.fromCallable {
            val realm= Realm.getInstance(QuestionConfiguration().config())
            val questions=ArrayList<Question>()
            realm.use {
               val questionObj= realm.where(QuestionRealmObject::class.java)
                    .findAll()
                println(questionObj.size)

                questionObj.forEach {
                    questions.add(Question(it.question,it.answer))
                }
            }
            questions
        }
    }
}