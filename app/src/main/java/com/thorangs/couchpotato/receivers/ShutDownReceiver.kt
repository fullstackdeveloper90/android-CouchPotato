package com.thorangs.couchpotato.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.services.StepCounterService
import com.thorangs.couchpotato.utils.Logger
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Laxman Bhattarai on 11/15/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class ShutDownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SHUTDOWN) {
            Logger.log("shutting down")
            val intentToStartService = Intent(context, StepCounterService::class.java)
            intentToStartService.putExtra("force_update",true)
            context.stopService(intentToStartService)
            context.startService(intentToStartService)
            context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                    .putBoolean("correctShutdown", true).apply()
            val db = PedometerApp.stepLogFactory()
            Single.fromCallable {

            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe()

        }
    }
}