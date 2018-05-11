package com.thorangs.couchpotato.models.user

import com.google.gson.annotations.SerializedName

/**
 ** Created by nbnsresta on 11/23/2017.
 **/
class PasswordChangeUser(@SerializedName("email") val email: String,
                         @SerializedName("oldPassword") val oldPassword: String,
                         @SerializedName("newPassword") val newPassword: String
)