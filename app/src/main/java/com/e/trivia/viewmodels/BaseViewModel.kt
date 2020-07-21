package com.e.trivia.viewmodels

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel: ViewModel() {

   protected val compositeDisposable=CompositeDisposable()

   protected inline operator fun <reified T:Disposable> T.unaryPlus(){
        compositeDisposable.add(this)
   }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}