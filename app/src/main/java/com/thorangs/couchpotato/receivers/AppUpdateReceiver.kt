package com.thorangs.couchpotato.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thorangs.couchpotato.services.StepCounterService
import com.thorangs.couchpotato.utils.Logger

/**
 * Created by Laxman Bhattarai on 11/15/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class AppUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            Logger.log("app updated")
            context.startService(Intent(context, StepCounterService::class.java))
        }
    }
}