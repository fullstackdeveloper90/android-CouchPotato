package com.thorangs.couchpotato.database

import android.content.SharedPreferences
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.backend.syncing.Step
import com.thorangs.couchpotato.utils.DateUtils
import com.thorangs.couchpotato.utils.Logger
import com.thorangs.couchpotato.utils.flooredCalendar
import com.thorangs.couchpotato.utils.today
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by Laxman Bhattarai on 11/11/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class StepLogFactory(private val prefs: SharedPreferences, private val logsDAO: StepsLogDAO) : IStepLogFactory {

    private val stepsSyncService = RestClient.instance().stepSyncService

    override fun getSteps(date: Long): Flowable<StepsLog> {
        return this.logsDAO.logWithDate(date)
    }

    override fun getAllSteps(): Flowable<List<StepsLog>> {
        return this.logsDAO.getAll()
    }

    override fun getNonReactiveSteps(date: Long): StepsLog {
        return this.logsDAO.nonReactiveLog(date)
    }

    override fun getAllSteps(startDate: Long, endDate: Long): Flowable<List<StepsLog>> {
        return this.logsDAO.logsBetweenDates(startDate, endDate)
    }

    override fun getAllStepsNonReactive(startDate: Long, endDate: Long): List<StepsLog> {
        return this.logsDAO.logsBetweenDatesNonReactive(startDate, endDate)
    }

    override fun stepsWithGivenSizeNonReactive(limit: Int): List<StepsLog> {
        return this.logsDAO.logsWithCountLimitNonReactive(limit)
    }

    override fun stepsWithGivenSize(limit: Int): Flowable<List<StepsLog>> {
        return this.logsDAO.logsWithCountLimit(limit)
    }

    override fun saveSteps(log: StepsLog) {
        Single.fromCallable {
            this.logsDAO.insertOrUpdate(log)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()

    }

    override fun saveAllSteps(logs: List<StepsLog>) {
        this.logsDAO.insertOrUpdateAll(logs)
    }


    override fun updateTargetSteps(target: Int) {
        Single.fromCallable {
            var todaySteps = getNonReactiveSteps(today)
            if (todaySteps == null) todaySteps = StepsLog(today, targetSteps = getCurrentTarget())
            val newStep = todaySteps.copy(targetSteps = target)
            this.logsDAO.insertOrUpdate(newStep)
            setCurrentTarget(target)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn { it.message }
                .subscribe()
    }

    override fun removeInvalidEntries() {
        Single.fromCallable {
            this.logsDAO.deleteInvalidEntries()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()

    }

    override fun daysCount(): Int {
        return this.logsDAO.daysCount()
    }

    override fun removeNegativeEntries() {
        Single.fromCallable {
            this.logsDAO.deleteNegativeEntries()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    override fun deleteSteps(log: StepsLog) {
        Single.fromCallable {
            this.logsDAO.delete(log)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    override fun addToSpecificEntry(steps: Int, date: Long) {
        Single.fromCallable {
            var temp = this.logsDAO.nonReactiveLog(date)
            if (temp == null) temp = StepsLog(date, targetSteps = getCurrentTarget())
            this.logsDAO.insertOrUpdate(StepsLog(date, temp.steps + steps, temp.targetSteps))
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()

    }

    override fun getCurrentTarget(): Int {
        return prefs.getInt("target_steps", 10000)
    }

    override fun setCurrentTarget(target: Int) {
        prefs.edit().putInt("target_steps", target).apply()
    }

    override fun getSavedStepsSinceLastBoot(): Int {
        return prefs.getInt("saved_steps", Int.MIN_VALUE)
    }

    override fun saveStepsSinceLastBoot(steps: Int): Boolean {
        val successful = prefs.edit().putInt("saved_steps", steps).commit()
        if (!successful) saveStepsSinceLastBoot(steps)
        return true
    }

    override fun getLastSavedTime(): Long {
        return prefs.getLong("last_saved_time", Long.MIN_VALUE)
    }

    override fun saveLastSavedTime(time: Long): Boolean {
        val successful = prefs.edit().putLong("last_saved_time", time).commit()
        if (!successful) saveLastSavedTime(time)
        return true
    }

    override fun addToLastEntry(steps: Int) {
        Single.fromCallable {
            val lastEntry = getLastEntry()
            this.logsDAO.insertOrUpdate(lastEntry.copy(steps = lastEntry.steps + steps))
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()

    }

    override fun getLastEntry(): StepsLog {
        return this.logsDAO.lastLogEntryNonReactive()
    }

    override fun getUserId(): Long {
        return prefs.getLong("user_id", 0)
    }

    override fun saveUserId(id: Long): Boolean {
        val successful = prefs.edit().putLong("user_id", id).commit()
        if (!successful) saveUserId(id)
        return true

    }

    override fun getUserEmail(): String {
        return prefs.getString("user_email", "")
    }

    override fun saveUserEmail(email: String): Boolean {
        val successful = prefs.edit().putString("user_email", email).commit()
        if (!successful) saveUserEmail(email)
        return true
    }

    override fun getBuddyEmail(): String {
        return prefs.getString("buddy_email", "")
    }

    override fun saveBuddyEmail(buddyEmail: String): Boolean {
        val successful = prefs.edit().putString("buddy_email", buddyEmail).commit()
        if (!successful) saveBuddyEmail(buddyEmail)
        return true
    }

    override fun syncLocalAndCloud() {
        if (getUserId() > 0) {
            Single.fromCallable {
                val step = getNonReactiveSteps(today)
                val calendar = Calendar.getInstance()
                if (step != null) {
                    calendar.timeInMillis = step.date
                    val dateForService = DateUtils.getEnglishDateString(calendar)
                    val stepToSend = Step(getUserId(), dateForService, step.steps, step.targetSteps)

                    stepsSyncService.saveSteps(stepToSend).enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
                            Logger.log("retrofit success " + response.toString())
                            Logger.log("retrofit success message is " + response?.body().toString())
                        }

                        override fun onFailure(call: Call<String?>?, t: Throwable?) {
                            Logger.log("retrofit success " + t?.message)

                        }
                    })
                }
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe()
        }

    }

    override fun setNotifyDaily(notifyDaily: Boolean): Boolean {
        val successful = prefs.edit().putBoolean("notify_daily", notifyDaily).commit()
        if (!successful) setNotifyDaily(notifyDaily)
        return true
    }

    override fun getNotifyDaily(): Boolean {
        return prefs.getBoolean("notify_daily", true)
    }

    override fun clearAllPreferences(): Boolean {
        return prefs.edit().clear().commit()
    }

    override fun stepsInYearByMonth(year: Int): Map<Int, Int> {
        val monthlySteps = mutableMapOf<Int, Int>()
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        val startingDayOfYear = flooredCalendar(c.timeInMillis)
        startingDayOfYear.set(Calendar.DAY_OF_YEAR, 0)
        val startingDayMS = startingDayOfYear.timeInMillis
        startingDayOfYear.add(Calendar.YEAR, 1)
        val endingDayMS = startingDayOfYear.timeInMillis
        val values = getAllStepsNonReactive(startingDayMS, endingDayMS)
        val calendar = Calendar.getInstance()
        values.forEach { value ->
            calendar.timeInMillis = value.date
            if (monthlySteps[calendar.get(Calendar.MONTH)] == null) {
                monthlySteps[calendar.get(Calendar.MONTH)] = 0
            }
            monthlySteps[calendar.get(Calendar.MONTH)] = monthlySteps[calendar.get(Calendar.MONTH)]!!.plus(value.steps)
        }
        return monthlySteps
    }

    override fun getUserName(): String {
        return prefs.getString("user_name", "")
    }

    override fun saveUserName(name: String): Boolean {
        return prefs.edit().putString("user_name", name).commit()
    }

    override fun getCreatedDate(): String {
        return prefs.getString("created_date", "1970-01-01")
    }

    override fun saveCreatedDate(date: String): Boolean {
        return prefs.edit().putString("created_date", date).commit()
    }

    override fun getPurchasedDate(): String {
        return prefs.getString("purchase_date", "1970-01-01")
    }

    override fun savePurchasedDate(date: String): Boolean {
        return prefs.edit().putString("purchase_date", date).commit()
    }

    override fun deleteSteps() {
        this.logsDAO.dropStepsTable()
    }
}