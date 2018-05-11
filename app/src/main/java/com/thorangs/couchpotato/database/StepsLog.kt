package com.thorangs.couchpotato.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Laxman Bhattarai on 11/11/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
@Entity(tableName = "steps_log")
data class StepsLog(
                @PrimaryKey(autoGenerate = false)
                @SerializedName("date")
                val date: Long,
                @SerializedName("steps")
                val steps: Int = 0,
                @SerializedName("stepsTarget")
                val targetSteps: Int)


