package com.example.rentlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.model.RentLog

@Database(
    entities = [Landlord::class, RentEntry::class, RentLog::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun landlordDao(): LandlordDao
    abstract fun rentEntryDao(): RentEntryDao
    abstract fun rentLogDao(): RentLogDao
}
