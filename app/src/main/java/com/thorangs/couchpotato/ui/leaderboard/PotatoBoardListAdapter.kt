package com.thorangs.couchpotato.ui.leaderboard

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.util.SortedListAdapterCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.models.user.PotatoUser

class PotatoBoardListAdapter : RecyclerView.Adapter<PotatoBoardListAdapter.PotatoBoardListViewHolder>() {

    private val sortedListCallBack = object : SortedListAdapterCallback<PotatoUser>(this) {
        override fun areContentsTheSame(oldItem: PotatoUser?, newItem: PotatoUser?) = (oldItem?.id == newItem?.id)

        override fun compare(o1: PotatoUser?, o2: PotatoUser?) = (o2?.stepsMissed!! - o1?.stepsMissed!!)

        override fun areItemsTheSame(item1: PotatoUser?, item2: PotatoUser?) = (item1?.id == item2?.id)
    }

    private var dataList: SortedList<PotatoUser> = SortedList(PotatoUser::class.java, sortedListCallBack)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PotatoBoardListViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.potato_list_row, parent, false)
        return PotatoBoardListViewHolder(v)
    }

    override fun onBindViewHolder(holder: PotatoBoardListViewHolder?, position: Int) {
        holder?.apply {
            txtName.text = String.format("%d. %s", position + 1, dataList[position].name)
            txtGoalsMissed.text = dataList[position].stepsMissed.toString()
        }
    }

    fun setData(users: List<PotatoUser>) {
        dataList.clear()
        dataList.beginBatchedUpdates()
        dataList.addAll(users)
        dataList.endBatchedUpdates()
        notifyItemRangeChanged(0, dataList.size())
    }

    fun addData(users: List<PotatoUser>) {
        dataList.beginBatchedUpdates()
        dataList.addAll(users)
        dataList.endBatchedUpdates()
        notifyItemRangeChanged(0, dataList.size())
    }

    override fun getItemCount() = dataList.size()

    inner class PotatoBoardListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txt_potato_name)
        val txtGoalsMissed: TextView = view.findViewById(R.id.txt_potato_goals_missed)
    }
}