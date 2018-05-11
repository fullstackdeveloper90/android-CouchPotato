package com.thorangs.couchpotato.database

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by Laxman Bhattarai on 11/12/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
@Dao
interface StepsLogDAO {

    @Query("SELECT * FROM steps_log ORDER BY date")
    fun getAll(): Flowable<List<StepsLog>>

    @Query("SELECT COUNT(*) from steps_log")
    fun daysCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(log: StepsLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateAll(logs: List<StepsLog>)

    @Delete
    fun delete(log: StepsLog)

    @Query("SELECT * FROM steps_log WHERE date = :date")
    fun logWithDate(date: Long): Flowable<StepsLog>

    @Query("SELECT * FROM steps_log WHERE date = :date")
    fun nonReactiveLog(date: Long): StepsLog

    @Query("SELECT * FROM steps_log ORDER BY date DESC LIMIT :limit")
    fun logsWithCountLimit(limit: Int): Flowable<List<StepsLog>>

    @Query("SELECT * FROM steps_log ORDER BY date ASC LIMIT :limit")
    fun logsWithCountLimitNonReactive(limit: Int): List<StepsLog>

    @Query("SELECT * FROM steps_log ORDER BY date DESC LIMIT 1")
    fun lastLogEntryNonReactive(): StepsLog

    @Query("DELETE FROM steps_log WHERE steps <= 0")
    fun deleteNegativeEntries()

    @Query("DELETE FROM steps_log WHERE steps >= 30000")
    fun deleteInvalidEntries()

    @Query("UPDATE steps_log SET steps = steps + :steps  WHERE date = :date")
    fun addToEntry(date: Long, steps: Int)

    @Query("SELECT * FROM steps_log WHERE date >= :start AND date<=:end")
    fun logsBetweenDates(start: Long, end: Long): Flowable<List<StepsLog>>

    @Query("SELECT * FROM steps_log WHERE date >= :start AND date<=:end")
    fun logsBetweenDatesNonReactive(start: Long, end: Long): List<StepsLog>

    @Query("DELETE FROM steps_log")
    fun dropStepsTable()

}