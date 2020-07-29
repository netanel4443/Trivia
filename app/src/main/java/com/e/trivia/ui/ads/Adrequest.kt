package com.e.VoiceAssistant.ui.ads

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.*

class Adrequest {

    fun request():AdRequest{

        val adRequest=AdRequest.Builder().build()

        return adRequest
    }
}