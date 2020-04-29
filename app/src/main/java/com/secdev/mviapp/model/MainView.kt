package com.secdev.mviapp.model

import com.hannesdorfmann.mosby3.mvp.MvpView
import com.secdev.mviapp.view.MainViewState
import io.reactivex.Observable

interface MainView : MvpView {
    val imageIntent : Observable<Int>

    fun render(viewState: MainViewState)
}