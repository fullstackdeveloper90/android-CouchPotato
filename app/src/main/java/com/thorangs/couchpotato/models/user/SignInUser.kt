package com.thorangs.couchpotato.models.user

import com.google.gson.annotations.SerializedName

/**
 ** Created by nbnsresta on 11/23/2017.
 **/
data class SignInUser (
        @SerializedName("email") val email:String,
        @SerializedName("password") val password:String
)