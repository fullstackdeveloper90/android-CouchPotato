package com.thorangs.couchpotato.ui.records

import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.database.StepsLog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

/**
 * Created by Laxman Bhattarai on 11/18/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class RecordsPresenter(val view: RecordsContract.View) : RecordsContract.Presenter {

    private val stepLogFactory = PedometerApp.stepLogFactory()!!
    private val disposables = CompositeDisposable()

    override fun retrieveData() {
        val d: DisposableObserver<List<StepsLog>> = getDisposableMonthlyRashiFalObserver()
       stepLogFactory.stepsWithGivenSize(100)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), false, 100)
                .subscribe{view.showData(it)}
       disposables.add(d)
    }


    private fun getDisposableMonthlyRashiFalObserver(): DisposableObserver<List<StepsLog>> {

        return object : DisposableObserver<List<StepsLog>>() {
            override fun onNext(value: List<StepsLog>) {
                view.showData(value.reversed())
            }
            override fun onError(e: Throwable) {}
            override fun onComplete() {
            }
        }
    }

    override fun finalize() {
        disposables.dispose()
    }

}