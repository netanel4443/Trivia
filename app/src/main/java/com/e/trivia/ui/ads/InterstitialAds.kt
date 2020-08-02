package com.e.trivia.ui.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class InterstitialAds  constructor(val context: Context) {
   private val mInterstitialAd: InterstitialAd = InterstitialAd(context)
   private val fakeAdUnitId= "ca-app-pub-3940256099942544/1033173712" /** a test unitId*/

    var onClosedAction:(()->Unit)?=null

    init {

        mInterstitialAd.adUnitId = fakeAdUnitId

        mInterstitialAd.adListener=object: AdListener(){

            override fun onAdClosed() {
                super.onAdClosed()
                loadInterstitialAd()// load for future use
                onClosedAction?.invoke()
            }
        }
            loadInterstitialAd()//load ad for the first time when initializing this class.
    }

    fun show(){
        if (mInterstitialAd.isLoaded)
            mInterstitialAd.show()
    }

    private fun loadInterstitialAd(){
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    fun removeListener(){
        mInterstitialAd.adListener=null
    }
}