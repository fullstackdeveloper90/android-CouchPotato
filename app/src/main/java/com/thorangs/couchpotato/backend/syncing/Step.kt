package com.thorangs.couchpotato.backend.syncing

import com.google.gson.annotations.SerializedName

/**
 * Created by Laxman Bhattarai on 11/19/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
data class Step(
        @SerializedName("userId")
        val userId: Long,
        @SerializedName("createdDate")
        val createdDate: String = "",
        @SerializedName("steps")
        val steps: Int = 0,
        @SerializedName("stepsTarget")
        val stepsTarget: Int = 10000
)