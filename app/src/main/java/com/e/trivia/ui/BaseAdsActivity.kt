package com.e.trivia.ui

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseAdsActivity :AppCompatActivity(){
    protected val compositeDisposable= CompositeDisposable()

    protected inline operator fun <reified T: Disposable> T.unaryPlus(){
        compositeDisposable.add(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}