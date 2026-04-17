package com.example.rentlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rentlog.domain.model.Landlord
import com.example.rentlog.domain.model.RentEntry

@Database(
    entities = [Landlord::class, RentEntry::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun landlordDao(): LandlordDao
    abstract fun rentEntryDao(): RentEntryDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE rent_entries ADD COLUMN paymentMode TEXT NOT NULL DEFAULT 'UPI'"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS rent_logs")
            }
        }
    }
}
