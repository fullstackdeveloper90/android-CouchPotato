package com.thorangs.couchpotato

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import com.thorangs.couchpotato.database.CPDatabase
import com.thorangs.couchpotato.database.StepLogFactory
import com.thorangs.couchpotato.receivers.AlarmReceiver
import com.thorangs.couchpotato.services.StepCounterService
import com.thorangs.couchpotato.utils.today
import java.util.*

/**
 * Created by Laxman Bhattarai on 11/11/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class PedometerApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        val prefs = getSharedPreferences("couch_potato", Context.MODE_PRIVATE)
        val stepsDAO = CPDatabase.createPersistentDatabase(this).logs()
        stepLogFactory = StepLogFactory(prefs, stepsDAO)
        initDataBase()
        if (Build.VERSION.SDK_INT <= 26) {
            setupAlarmForCloudSyncingNewDay()
        }
        setUpRepeatingAlarm()
    }

    private fun cancelRepeatingAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmManager.cancel(sender)
    }

    private fun setUpRepeatingAlarm() {
        cancelRepeatingAlarm()
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 44)
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                1000 * 60 * 15, alarmIntent)
    }

    private fun setUpForDayEnd() {

    }

    companion object {
        private var stepLogFactory: StepLogFactory? = null
        fun stepLogFactory() = stepLogFactory
    }

    private fun initDataBase() {
        stepLogFactory()?.addToSpecificEntry(0, today)
    }

    private fun setupAlarmForCloudSyncingNewDay() {
        setUpForDayClosing()
        setUpForNewDay()
    }

    private fun setUpForNewDay() {
        val dayAlarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentToStartService = Intent(this, StepCounterService::class.java)
        intentToStartService.putExtra("force_update", true)
        intentToStartService.putExtra("from_alarm", true)
        val alarmIntent = PendingIntent.getService(this, 0, intentToStartService, 0)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() //+ TimeUnit.MINUTES.toMillis(1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 10)
        dayAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)

    }

    private fun setUpForDayClosing() {
        val dayAlarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentToStartService = Intent(this, StepCounterService::class.java)
        intentToStartService.putExtra("force_update", true)
        intentToStartService.putExtra("from_alarm", true)
        val alarmIntent = PendingIntent.getService(this, 0, intentToStartService, 0)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() //+ TimeUnit.MINUTES.toMillis(1)
        calendar.add(Calendar.DATE, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 50)
        dayAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)

    }

}