package com.e.trivia.data.repo.configurations

import io.realm.RealmConfiguration

class QuestionConfiguration constructor(){
    fun config(): RealmConfiguration {
      val config=  RealmConfiguration.Builder()
            .assetFile("questions.realm")
            //.readOnly()
            .build()
      return config
    }
}