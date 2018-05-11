package com.thorangs.couchpotato.utils

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build

@TargetApi(Build.VERSION_CODES.M)
object API23Wrapper {

    fun requestPermission(a: Activity, permissions: Array<String>) {
        a.requestPermissions(permissions, 42)
    }

}
