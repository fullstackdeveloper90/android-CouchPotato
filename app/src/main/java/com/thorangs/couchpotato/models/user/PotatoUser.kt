package com.thorangs.couchpotato.models.user

import com.google.gson.annotations.SerializedName

data class PotatoUser(
        @SerializedName("userId")
        val id: Long,

        @SerializedName("name")
        val name: String,

        @SerializedName("totalMissedSteps")
        val stepsMissed: Int
)