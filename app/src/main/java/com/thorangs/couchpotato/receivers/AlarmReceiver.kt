package com.thorangs.couchpotato.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.thorangs.couchpotato.BuildConfig
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.services.StepCounterService
import com.thorangs.couchpotato.utils.Logger
import com.thorangs.couchpotato.utils.isInternetConnected
import com.thorangs.couchpotato.utils.today

@Suppress("PrivatePropertyName")
/**
 * Created by Laxman Bhattarai on 12/5/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class AlarmReceiver : BroadcastReceiver(), SensorEventListener {
    private var steps: Int = 0
    private lateinit var context: Context
    val db = PedometerApp.stepLogFactory()!!

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        Logger.log("inside on sensor changed")
        if (event != null) {
            if (event.values[0] < Integer.MAX_VALUE) {
                steps = event.values[0].toInt()
                handleStepSaving()
            }
        }
    }

    @Synchronized
    private fun handleStepSaving() {
        if (steps > db.getSavedStepsSinceLastBoot()) {
            val stepsToSave = steps - db.getSavedStepsSinceLastBoot()
            db.addToSpecificEntry(stepsToSave, today)
            db.saveLastSavedTime(System.currentTimeMillis())
            db.saveStepsSinceLastBoot(steps)
            if (isInternetConnected(context)) {
                db.syncLocalAndCloud()
            }
        }
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        try {
            sm.unregisterListener(this)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.log(e)
            e.printStackTrace()
        }


    }

    override fun onReceive(context: Context, intent: Intent) {
        Logger.log("Alarm Received ")
        this.context = context
        if (!StepCounterService.isSerViceRunning) {
            registerSensor()
        }
    }

    private fun registerSensor() {
        Logger.log("re-register sensor listener")
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        try {
            sm.unregisterListener(this)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.log(e)
            e.printStackTrace()
        }

        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_UI, (5 * MICROSECONDS_IN_ONE_MINUTE))
    }


    private val MICROSECONDS_IN_ONE_MINUTE = 60_000_000

}