package com.e.trivia.ui

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.e.VoiceAssistant.ui.ads.Adrequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseAdsActivity :AppCompatActivity(){
    protected val compositeDisposable= CompositeDisposable()
    private var ad: AdView?=null

    protected inline operator fun <reified T: Disposable> T.unaryPlus(){
        compositeDisposable.add(this)
    }



    protected fun loadAd(adLayout: FrameLayout, adUnitId:String){
        val ad=AdView(this)
        ad.adSize= AdSize.BANNER
        ad.adUnitId=adUnitId
        adLayout.addView(ad)
        ad.loadAd(Adrequest().request())
    }
    override fun onResume() {
        super.onResume()
        ad?.resume()
    }

    override fun onPause() {
        super.onPause()
        ad?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    protected val fakeUnitId: String  = "ca-app-pub-3940256099942544/6300978111"

}