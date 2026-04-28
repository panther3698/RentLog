package com.devchiradhi.rentlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devchiradhi.rentlog.domain.model.Landlord
import com.devchiradhi.rentlog.domain.model.RentEntry

@Database(
    entities = [Landlord::class, RentEntry::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun landlordDao(): LandlordDao
    abstract fun rentEntryDao(): RentEntryDao

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
