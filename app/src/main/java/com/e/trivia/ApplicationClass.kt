package com.e.trivia

import android.app.Application
import io.realm.Realm

class ApplicationClass :Application(){

    override fun onCreate() {
        super.onCreate()
        //init database
        Realm.init(this)


    }
}