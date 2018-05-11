package com.thorangs.couchpotato.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.services.StepCounterService
import com.thorangs.couchpotato.utils.Logger

/**
 * Created by Laxman Bhattarai on 11/15/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman receivers
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Logger.log("phone booted ")
        Toast.makeText(context,"Phone booted",Toast.LENGTH_LONG).show()
        if(intent.action==Intent.ACTION_BOOT_COMPLETED) {
            Logger.log("booted")
            val prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE)
            val db = PedometerApp.stepLogFactory()!!
            if (!prefs.getBoolean("correctShutdown", false)) {
                Logger.log("Incorrect shutdown")
                val steps = db.getSavedStepsSinceLastBoot()
                Logger.log("Trying to recover $steps steps")
                db.addToLastEntry(steps)
            }
            db.removeNegativeEntries()
            db.saveStepsSinceLastBoot(0)
            prefs.edit().remove("correctShutdown").apply()
            val intentToStartService = Intent(context, StepCounterService::class.java)
            intentToStartService.putExtra("force_update",true)
            context.stopService(intentToStartService)
            context.startService(intentToStartService)
        }
    }
}