package com.example.rentlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.model.RentEntry
import com.example.rentlog.domain.model.RentLog

@Database(
    entities = [Landlord::class, RentEntry::class, RentLog::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun landlordDao(): LandlordDao
    abstract fun rentEntryDao(): RentEntryDao
    abstract fun rentLogDao(): RentLogDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE rent_entries ADD COLUMN attachmentUri TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
