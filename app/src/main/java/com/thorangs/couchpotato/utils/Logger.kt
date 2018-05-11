package com.thorangs.couchpotato.utils

import android.util.Log
import com.thorangs.couchpotato.BuildConfig


object Logger {

    private val APP = "datatest"

    fun log(ex: Throwable) {
        log(ex.message!!)
        for (ste in ex.stackTrace) {
            log(ste.toString())
        }
    }

    fun log(msg: String) {
        if (BuildConfig.DEBUG) Log.d(APP, msg)
    }

}
