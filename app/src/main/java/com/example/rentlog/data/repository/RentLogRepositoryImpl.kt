package com.example.rentlog.data.repository

import com.example.rentlog.data.local.RentLogDao
import com.example.rentlog.domain.model.RentLog
import com.example.rentlog.domain.repository.RentLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RentLogRepositoryImpl @Inject constructor(
    private val dao: RentLogDao
) : RentLogRepository {
    override fun getAllLogs(): Flow<List<RentLog>> = dao.getAllLogs()

    override suspend fun insertLog(log: RentLog) {
        dao.insertLog(log)
    }

    override suspend fun deleteLog(log: RentLog) {
        dao.deleteLog(log)
    }
}
