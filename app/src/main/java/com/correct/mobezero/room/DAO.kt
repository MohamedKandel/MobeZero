package com.correct.mobezero.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(call: CallLog)

    @Query("select * from Calls")
    suspend fun callLog(): List<CallLog>?

    @Query("delete from Calls where call_ID = :id")
    suspend fun deleteCall(id: Int)

    @Query("delete from Calls where 1")
    suspend fun deleteAllCalls()

    @Query("select MAX(call_ID) from Calls")
    suspend fun getNextID(): Int
}