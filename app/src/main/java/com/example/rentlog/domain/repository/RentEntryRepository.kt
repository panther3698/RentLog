package com.devchiradhi.rentlog.domain.repository

import com.devchiradhi.rentlog.domain.model.RentEntry
import kotlinx.coroutines.flow.Flow

interface RentEntryRepository {
    fun getEntriesForYear(year: Int): Flow<List<RentEntry>>
    fun getEntriesForYearAndLandlord(year: Int, landlordId: Int): Flow<List<RentEntry>>
    fun getYearlyTotal(year: Int): Flow<Double?>
    fun getEntriesForLandlord(landlordId: Int): Flow<List<RentEntry>>
    suspend fun insertOrUpdateRentEntry(entry: RentEntry): Long
    suspend fun deleteRentEntry(entry: RentEntry)
}
