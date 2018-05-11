package com.thorangs.couchpotato.ui.records

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.database.StepsLog

class RecordsView : Fragment(), RecordsContract.View {

    private lateinit var topTab: View
    private val recordAdapter: RecordsRecyclerAdapter = RecordsRecyclerAdapter()
    private val presenter: RecordsContract.Presenter = RecordsPresenter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_records, container, false)
        rootView.apply {
            topTab = findViewById(R.id.swipe_tab_top)
        }
        val recordList = rootView.findViewById<RecyclerView>(R.id.record_list)
        recordList.isNestedScrollingEnabled = false
        recordList.layoutManager = LinearLayoutManager(rootView.context)
        recordList.adapter = recordAdapter
        presenter.retrieveData()
        return rootView
    }

    override fun showData(dataList: List<StepsLog>) {
        val newList = dataList.toMutableList()
        if (newList.size > 0) newList.removeAt(0)
        recordAdapter.replaceData(newList)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.finalize()
    }
}