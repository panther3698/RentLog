package com.example.rentlog.domain.usecase

import com.example.rentlog.domain.model.RentLog
import com.example.rentlog.domain.repository.RentLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRentLogsUseCase @Inject constructor(
    private val repository: RentLogRepository
) {
    operator fun invoke(): Flow<List<RentLog>> = repository.getAllLogs()
}
