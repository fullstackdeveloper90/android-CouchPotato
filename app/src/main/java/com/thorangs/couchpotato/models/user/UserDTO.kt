package com.thorangs.couchpotato.models.user

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserDTO(
        @SerializedName("id")
        val id: Long,
        @SerializedName("name")
        val name: String,
        @SerializedName("email")
        val email: String,
        @SerializedName("dailyStepsTarget")
        val targetSteps: Int,
        @SerializedName("buddyEmail")
        val buddyEmail: String,
        @SerializedName("notifyFrequency")
        val notifyFrequency: String = "DAILY",
        @SerializedName("createdDate")
        val createdDate: String


) : Serializable
