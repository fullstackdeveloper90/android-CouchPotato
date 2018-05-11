package com.thorangs.couchpotato.models.user

import com.google.gson.annotations.SerializedName

/**
 ** Created by nbnsresta on 11/23/2017.
 **/
data class SignUpUser(
        @SerializedName("name") val name: String,
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String,
        @SerializedName("gmtTimeOffset") val gmtTimezoneOffset: Int
)