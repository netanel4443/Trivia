package com.e.trivia

import android.app.Application
import com.google.android.gms.ads.MobileAds
import io.realm.Realm

class ApplicationClass :Application(){

    override fun onCreate() {
        super.onCreate()
        //init database
        Realm.init(this)
        MobileAds.initialize(this) {}


    }
}