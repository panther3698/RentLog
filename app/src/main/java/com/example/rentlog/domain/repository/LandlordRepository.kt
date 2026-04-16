package com.example.rentlog.domain.repository

import com.example.rentlog.domain.model.Landlord
import kotlinx.coroutines.flow.Flow

interface LandlordRepository {
    fun getAllLandlords(): Flow<List<Landlord>>
    suspend fun getLandlordById(id: Int): Landlord?
    suspend fun insertOrUpdateLandlord(landlord: Landlord): Long
    suspend fun deleteLandlord(landlord: Landlord)
}
