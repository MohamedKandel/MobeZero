package com.correct.mobezero.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CallLog::class], version = 1)
abstract class CallsDB : RoomDatabase() {
    abstract fun dao(): DAO

    companion object {
        @Volatile
        private var INSTANCE: CallsDB? = null
        fun getDBInstance(context: Context): CallsDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallsDB::class.java,
                    "CallsDB"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}