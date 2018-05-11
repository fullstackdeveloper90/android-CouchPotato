package com.thorangs.couchpotato.ui.records

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.database.StepsLog
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Laxman Bhattarai on 11/18/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class RecordsRecyclerAdapter : RecyclerView.Adapter<RecordsRecyclerAdapter.RecordsViewHolder>() {

    var recordsList: List<StepsLog> = listOf()

    override fun onBindViewHolder(holderRecord: RecordsRecyclerAdapter.RecordsViewHolder, position: Int) {
        holderRecord.bindView(recordsList[position])
    }


    fun replaceData(record: List<StepsLog>) {
        this.recordsList = record
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, index: Int): RecordsRecyclerAdapter.RecordsViewHolder {
        val item = recordsList[index]
        if (item.steps == 0 && item.date == 0L) {
            return TargetChangedVH(LayoutInflater.from(parent.context).inflate(R.layout.item_target_changed_vh, parent, false))

        }
        return RecordItemVH(LayoutInflater.from(parent.context).inflate(R.layout.item_record_vh, parent, false))
    }

    override fun getItemCount(): Int {
        return if (recordsList == null) 0 else recordsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    abstract class RecordsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bindView(log: StepsLog)
    }

    class TargetChangedVH(val view: View) : RecordsViewHolder(view) {
        private val goalUpdatedNotification: TextView = view.findViewById(R.id.goalUpdatedNotification)
        override fun bindView(log: StepsLog) {
            goalUpdatedNotification.text = view.context.getString(R.string.goal_updated_text) + "  ${log.targetSteps}"
        }
    }

    class RecordItemVH(val view: View) : RecordsViewHolder(view) {
        private val date: TextView = view.findViewById(R.id.date_item_record_vh)
        private val steps: TextView = view.findViewById(R.id.total_steps_item_record_vh)
        private val message: TextView = view.findViewById(R.id.message_steps_item_record_vh)

        override fun bindView(log: StepsLog) {
            if (log.steps >= log.targetSteps) {
                view.setBackgroundResource(R.drawable.bg_rounded_gradient)
                date.setTextColor(Color.WHITE)
                steps.setTextColor(Color.WHITE)
                message.setTextColor(Color.WHITE)
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = log.date
                date.text = SimpleDateFormat("MMM dd:").format(calendar.time)
                message.text = "${log.targetSteps} Daily goal reached!"
                steps.text = log.steps.toString()

            } else {
                view.setBackgroundResource(R.drawable.pale_white_rounded_drawable)
                date.setTextColor(ContextCompat.getColor(date.context, R.color.colorDark))
                steps.setTextColor(ContextCompat.getColor(date.context, R.color.colorDark))
                message.setTextColor(ContextCompat.getColor(date.context, R.color.colorDark))
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = log.date
                date.text = SimpleDateFormat("MMM dd:").format(calendar.time)
                message.text = "${log.targetSteps} Daily goal Missed."
                steps.text = log.steps.toString()
            }
        }
    }
}