package com.e.trivia.data.repo.configurations

import com.e.trivia.data.repo.modules.QuestionsRealmModule
import io.realm.RealmConfiguration

class QuestionConfiguration(){
    private  val module=QuestionsRealmModule()
    fun config(): RealmConfiguration {
      val config=  RealmConfiguration.Builder()
            .assetFile("questions.realm")
            .modules(module)
            .build()

      return config
    }
}