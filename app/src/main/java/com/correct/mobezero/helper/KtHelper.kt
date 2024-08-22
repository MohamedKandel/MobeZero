package com.correct.mobezero.helper

import com.correct.mobezero.room.CallLog
import com.correct.mobezero.room.DAO
import kotlinx.coroutines.runBlocking

object KtHelper {
    fun getNextIDBlocking(callsDao: DAO): Int {
        return runBlocking {
            callsDao.getNextID()+1
        }
    }

    fun insert(dao: DAO, callLog: CallLog) {
        runBlocking {
            dao.insert(callLog)
        }
    }
}