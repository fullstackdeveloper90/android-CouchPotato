package com.thorangs.couchpotato.ui.leaderboard

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.models.user.PotatoUser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class PotatoBoardView : Fragment() {

    private lateinit var potatoBoardListView: RecyclerView
    private val potatoAdapter = PotatoBoardListAdapter()

    private val apiService = RestClient.instance().potatoUserApiService
    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.potato_board, container, false)
        rootView?.apply {
            potatoBoardListView = findViewById(R.id.list_potato_board)
            potatoBoardListView.layoutManager = LinearLayoutManager(context)
            potatoBoardListView.adapter = potatoAdapter
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val observer = object : DisposableObserver<List<PotatoUser>>() {
            override fun onNext( t: List<PotatoUser>) {
                Log.d("PotatoBoard Contents" , "Contents size = "+t.size)
                potatoAdapter.setData(t)
            }

            override fun onComplete() {
                Log.d("PotatoBoard Contents" , "Contents fetch complete")
            }

            override fun onError(e: Throwable) {
                Log.d("PotatoBoard Contents" , e.message)
            }
        }

        apiService.getAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)

        disposable.add(observer)

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}