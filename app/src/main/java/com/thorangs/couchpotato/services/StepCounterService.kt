package com.thorangs.couchpotato.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.thorangs.couchpotato.BuildConfig
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.utils.Logger
import com.thorangs.couchpotato.utils.isInternetConnected
import com.thorangs.couchpotato.utils.today


/**
 * Created by Laxman Bhattarai on 11/15/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class StepCounterService : Service(), SensorEventListener {
    private var steps: Int = 0
    val db = PedometerApp.stepLogFactory()!!
    override fun onSensorChanged(event: SensorEvent) {
        Logger.log("inside on sensor changed")
        if (event.values[0] < Integer.MAX_VALUE) {
            steps = event.values[0].toInt()
            handleStepChange()
        }
    }

    @Synchronized
    private fun handleStepChange() {
        if (db.getSavedStepsSinceLastBoot() > steps && steps != 0) db.saveStepsSinceLastBoot(0)
        val stepsSaved = db.getSavedStepsSinceLastBoot()
        val lastSaved = db.getLastSavedTime()
        if (stepsSaved == Int.MIN_VALUE) db.saveStepsSinceLastBoot(steps)
        if (steps > (stepsSaved + SAVE_OFFSET_STEPS) || System.currentTimeMillis() > (lastSaved + SAVE_OFFSET_TIME)) {
            updateIfNecessary()
        }
    }

    private fun updateIfNecessary() {
        if (steps > db.getSavedStepsSinceLastBoot() && db.getSavedStepsSinceLastBoot() != Int.MIN_VALUE) {
            val stepsToSave = steps - db.getSavedStepsSinceLastBoot()
            db.addToSpecificEntry(stepsToSave, today)
            db.saveLastSavedTime(System.currentTimeMillis())
            db.saveStepsSinceLastBoot(steps)
            if (isInternetConnected(this)) {
                db.syncLocalAndCloud()
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Logger.log("SensorListener onCreate")
        reRegisterSensor()
    }

    private fun reRegisterSensor() {
        if (BuildConfig.DEBUG) Logger.log("re-register sensor listener")
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        try {
            sm.unregisterListener(this)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.log(e)
            e.printStackTrace()
        }

        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_UI, (5 * MICROSECONDS_IN_ONE_MINUTE))
        isSerViceRunning = true
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Logger.log("sensor service task removed")
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 3, Intent(this, StepCounterService::class.java), 0))
    }

    override fun onDestroy() {
        updateIfNecessary()
        super.onDestroy()
        Logger.log("SensorListener onDestroy")
        try {
            val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sm.unregisterListener(this)
            isSerViceRunning = false
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.log(e)
            e.printStackTrace()
        }

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    companion object {
        private val SAVE_OFFSET_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES
        private val SAVE_OFFSET_STEPS = 200
        private val MICROSECONDS_IN_ONE_MINUTE = 60_000_000
        public var isSerViceRunning = false

    }

    override fun onBind(intent: Intent) = null

}