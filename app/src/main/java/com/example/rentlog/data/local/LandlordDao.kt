package com.devchiradhi.rentlog.data.local

import androidx.room.*
import com.devchiradhi.rentlog.domain.model.Landlord
import kotlinx.coroutines.flow.Flow

@Dao
interface LandlordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLandlord(landlord: Landlord): Long

    @Query("SELECT * FROM landlords")
    fun getAllLandlords(): Flow<List<Landlord>>

    @Query("SELECT * FROM landlords WHERE id = :id")
    suspend fun getLandlordById(id: Int): Landlord?

    @Delete
    suspend fun deleteLandlord(landlord: Landlord)
}
