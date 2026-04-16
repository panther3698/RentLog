package com.example.rentlog.data.local

import androidx.room.*
import com.example.rentlog.domain.model.RentLog
import kotlinx.coroutines.flow.Flow

@Dao
interface RentLogDao {
    @Query("SELECT * FROM rent_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<RentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: RentLog)

    @Delete
    suspend fun deleteLog(log: RentLog)
}
