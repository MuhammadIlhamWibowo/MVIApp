package com.secdev.mviapp.view

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.secdev.mviapp.model.MainView
import com.secdev.mviapp.model.PartialMainState
import com.secdev.mviapp.utils.DataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class MainPresenter(
    internal var dataSource: DataSource
) : MviBasePresenter<MainView, MainViewState>() {
    override fun bindIntents() {
        val gotData = intent(ViewIntentBinder<MainView, Int> { it.imageIntent })
            .switchMap { index ->
                dataSource.getImageLinkFromList(index)
                    .map { imageLink -> PartialMainState.GotImageLink(imageLink) as PartialMainState }
                    .startWith(PartialMainState.Loading())
                    .onErrorReturn { error -> PartialMainState.Error(error) }
                    .subscribeOn(Schedulers.io())
            }

        val initState = MainViewState(
            isLoading = false,
            isImageViewShow = false,
            imageLink = "",
            error = null
        )

        val initIntent = gotData.observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(initIntent.scan(
            initState,
            BiFunction<MainViewState, PartialMainState, MainViewState> { prevState, changedState ->
                this.viewStateReducer(prevState, changedState)
            }
        ))
        { obj, viewState -> obj.render(viewState) }
    }

    internal fun viewStateReducer(
        prevState: MainViewState,
        changedState: PartialMainState
    ): MainViewState {
        if (changedState is PartialMainState.Loading) {
            prevState.isLoading = true
            prevState.isImageViewShow = false
        } else if (changedState is PartialMainState.GotImageLink) {
            prevState.isLoading = false
            prevState.isImageViewShow = true
            prevState.imageLink = changedState.imageLink
        } else if (changedState is PartialMainState.Error) {
            prevState.isLoading = false
            prevState.isImageViewShow = false
            prevState.error = changedState.error
        }

        return prevState
    }
}