package com.thorangs.couchpotato.ui.common

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v4.app.Fragment
import com.thorangs.couchpotato.BuildConfig
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.services.StepCounterService
import com.thorangs.couchpotato.utils.Logger
import com.thorangs.couchpotato.utils.today


/**
 * Created by Laxman Bhattarai on 11/17/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
open class ReactiveFragment : Fragment(), SensorEventListener {
    private var steps: Int = 0
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        Logger.log("inside on sensor changed")
        if (event.values[0] < Integer.MAX_VALUE) {
            steps = event.values[0].toInt()
            updateIfNecessary()
        }
    }

    override fun onResume() {
        super.onResume()
        unResisterService()
        registerSensor()
    }

    private fun registerService() {
        val intentToStartService = Intent(context, StepCounterService::class.java)
        intentToStartService.putExtra("force_update", true)
        context?.startService(intentToStartService)
    }

    private fun unResisterService() {
        val intentToStartService = Intent(context, StepCounterService::class.java)
        context?.stopService(intentToStartService)
    }

    private fun updateIfNecessary() {
        val db = PedometerApp.stepLogFactory()!!
        val stepsSaved = db.getSavedStepsSinceLastBoot()
        if (stepsSaved == Int.MIN_VALUE) db.saveStepsSinceLastBoot(steps)
        if (steps != 0 && stepsSaved > steps) {
            db.saveStepsSinceLastBoot(0)
        }
        val stepsToSave = steps - db.getSavedStepsSinceLastBoot()
        db.addToSpecificEntry(stepsToSave, today)
        db.saveLastSavedTime(System.currentTimeMillis())
        db.saveStepsSinceLastBoot(steps)
    }

    private fun registerSensor() {
        if (BuildConfig.DEBUG) Logger.log("re-register sensor listener")
        val sm = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        try {
            sm.unregisterListener(this)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.log(e)
            e.printStackTrace()
        }

        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_UI, 0)
    }

    override fun onPause() {
        super.onPause()
        Logger.log("SensorListener onDestroy")
        try {
            val sm = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sm.unregisterListener(this)
            registerService()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Logger.log(e)
            e.printStackTrace()
        }
    }
}