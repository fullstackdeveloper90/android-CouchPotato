package com.thorangs.couchpotato.database

import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName
import com.thorangs.couchpotato.utils.flooredCalendar
import com.thorangs.couchpotato.utils.today
import java.util.*

/**
 * Created by Laxman Bhattarai on 11/11/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
@Entity(tableName = "steps_log")
data class StepsLogDTO(
        @SerializedName("createdDate")
        private var createdDate: String,
        @SerializedName("steps")
        val steps: Int = 0,
        @SerializedName("stepsTarget")
        val targetSteps: Int) {
    fun getStepLog(): StepsLog {
        return StepsLog(getDateLongFromString(createdDate), this.steps, this.targetSteps)
    }

    private fun getDateLongFromString(createdDate: String): Long {
        if (createdDate.length != 10) throw NumberFormatException("String is not formatted for date")
        else {
            //2017-04-28
            val year = createdDate.substring(0, 4).toInt()
            val month = (createdDate.substring(5, 7).toInt()-1)
            val day = createdDate.substring(8, 10).toInt()
            val calendar = flooredCalendar(today)
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            return calendar.timeInMillis
        }
    }
}