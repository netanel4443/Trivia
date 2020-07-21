package com.e.trivia.ui.fragments

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment :Fragment(){

    protected val compositeDisposable=CompositeDisposable()

    protected inline operator fun <reified T:Disposable> T.unaryPlus(){
        compositeDisposable.add(this)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}