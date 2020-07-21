package com.e.trivia.data.repo.configurations

import io.realm.Realm
import io.realm.RealmConfiguration

class RealmMainConfiguration constructor(){
    fun config(): RealmConfiguration {
      val config=  RealmConfiguration.Builder()
            .name("mainfile.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
             Realm.setDefaultConfiguration(config)
      return config
    }
}