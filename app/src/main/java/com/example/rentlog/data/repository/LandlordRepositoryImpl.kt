package com.example.rentlog.data.repository

import com.example.rentlog.data.local.LandlordDao
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.repository.LandlordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LandlordRepositoryImpl @Inject constructor(
    private val dao: LandlordDao
) : LandlordRepository {
    override fun getAllLandlords(): Flow<List<Landlord>> = dao.getAllLandlords()
    
    override suspend fun getLandlordById(id: Int): Landlord? = dao.getLandlordById(id)
    
    override suspend fun insertOrUpdateLandlord(landlord: Landlord): Long = 
        dao.insertOrUpdateLandlord(landlord)
    
    override suspend fun deleteLandlord(landlord: Landlord) = dao.deleteLandlord(landlord)
}
