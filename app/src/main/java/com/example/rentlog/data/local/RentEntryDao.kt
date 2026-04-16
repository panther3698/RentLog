package com.example.rentlog.data.local

import androidx.room.*
import com.example.rentlog.domain.model.RentEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface RentEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRentEntry(entry: RentEntry): Long

    @Query("SELECT * FROM rent_entries WHERE year = :year ORDER BY month ASC")
    fun getEntriesForYear(year: Int): Flow<List<RentEntry>>

    @Query("SELECT SUM(amount) FROM rent_entries WHERE year = :year")
    fun getYearlyTotal(year: Int): Flow<Double?>

    @Query("SELECT * FROM rent_entries WHERE landlordId = :landlordId")
    fun getEntriesForLandlord(landlordId: Int): Flow<List<RentEntry>>

    @Delete
    suspend fun deleteRentEntry(entry: RentEntry)
}
