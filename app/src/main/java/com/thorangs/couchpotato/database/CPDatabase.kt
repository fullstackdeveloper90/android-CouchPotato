package com.thorangs.couchpotato.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * Created by Laxman Bhattarai on 11/12/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
@Database(
        entities = arrayOf(StepsLog::class),
        version = 1
)
public abstract class CPDatabase : RoomDatabase() {
    abstract fun logs(): StepsLogDAO

    companion object {
        private const val DB_NAME = "couchpotato.db"

        fun createInMemoryDatabase(context: Context): CPDatabase
                = Room.inMemoryDatabaseBuilder(context.applicationContext, CPDatabase::class.java).build()

        fun createPersistentDatabase(context: Context): CPDatabase
                = Room.databaseBuilder(context.applicationContext, CPDatabase::class.java, DB_NAME).build()
    }

}