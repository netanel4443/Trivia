package com.e.trivia.data.repo.configurations

import com.e.trivia.data.repo.modules.PlayerDetailsRealmModule
import com.e.trivia.data.repo.modules.QuestionsRealmModule
import io.realm.Realm
import io.realm.RealmConfiguration

class RealmMainConfiguration constructor(){
    private  val module= PlayerDetailsRealmModule()

    fun config(): RealmConfiguration {
      val config=  RealmConfiguration.Builder()
            .modules(module)
            .name("mainfile.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
             Realm.setDefaultConfiguration(config)
      return config
    }
}