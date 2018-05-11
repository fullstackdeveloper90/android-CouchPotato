package com.thorangs.couchpotato.database

import io.reactivex.Flowable

/**
 * Created by Laxman Bhattarai on 11/11/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
interface IStepLogFactory {
    fun getSteps(date: Long): Flowable<StepsLog>
    fun getNonReactiveSteps(date: Long): StepsLog
    fun getAllSteps(): Flowable<List<StepsLog>>
    fun getAllSteps(startDate: Long, endDate: Long): Flowable<List<StepsLog>>
    fun getAllStepsNonReactive(startDate: Long, endDate: Long): List<StepsLog>
    fun saveSteps(log: StepsLog)
    fun saveAllSteps(log: List<StepsLog>)
    fun updateTargetSteps(target: Int)
    fun removeInvalidEntries()
    fun removeNegativeEntries()
    fun daysCount(): Int
    fun deleteSteps(log: StepsLog)
    fun stepsWithGivenSizeNonReactive(limit: Int): List<StepsLog>
    fun stepsWithGivenSize(limit: Int): Flowable<List<StepsLog>>
    fun addToSpecificEntry(steps: Int, date: Long)
    fun getCurrentTarget(): Int
    fun setCurrentTarget(target: Int)
    fun getSavedStepsSinceLastBoot(): Int
    fun saveStepsSinceLastBoot(steps: Int): Boolean
    fun getLastSavedTime(): Long
    fun saveLastSavedTime(time: Long): Boolean
    fun syncLocalAndCloud()
    fun addToLastEntry(steps: Int)
    fun getLastEntry(): StepsLog
    fun getUserId(): Long
    fun saveUserId(id: Long): Boolean
    fun getUserEmail(): String
    fun saveUserEmail(email: String): Boolean
    fun getBuddyEmail(): String
    fun saveBuddyEmail(buddyEmail: String): Boolean
    fun setNotifyDaily(notifyDaily: Boolean): Any
    fun getNotifyDaily(): Boolean
    fun clearAllPreferences(): Boolean
    fun stepsInYearByMonth(year: Int): Map<Int, Int>
    fun getUserName(): String
    fun saveUserName(name: String): Boolean
    fun getCreatedDate(): String
    fun saveCreatedDate(date: String): Boolean
    fun deleteSteps()
    fun getPurchasedDate(): String
    fun savePurchasedDate(date: String): Boolean
}