package com.example.rentlog.data.repository

import com.example.rentlog.data.local.RentEntryDao
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.repository.RentEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RentEntryRepositoryImpl @Inject constructor(
    private val dao: RentEntryDao
) : RentEntryRepository {
    override fun getEntriesForYear(year: Int): Flow<List<RentEntry>> = dao.getEntriesForYear(year)
    
    override fun getYearlyTotal(year: Int): Flow<Double?> = dao.getYearlyTotal(year)
    
    override fun getEntriesForLandlord(landlordId: Int): Flow<List<RentEntry>> = 
        dao.getEntriesForLandlord(landlordId)
    
    override suspend fun insertOrUpdateRentEntry(entry: RentEntry): Long = 
        dao.insertOrUpdateRentEntry(entry)
    
    override suspend fun deleteRentEntry(entry: RentEntry) = dao.deleteRentEntry(entry)
}
