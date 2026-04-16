package com.example.rentlog.domain.repository

import com.example.rentlog.domain.model.RentLog
import kotlinx.coroutines.flow.Flow

interface RentLogRepository {
    fun getAllLogs(): Flow<List<RentLog>>
    suspend fun insertLog(log: RentLog)
    suspend fun deleteLog(log: RentLog)
}
