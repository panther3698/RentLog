package com.example.rentlog.di

import android.content.Context
import androidx.room.Room
import com.example.rentlog.data.local.AppDatabase
import com.example.rentlog.data.local.LandlordDao
import com.example.rentlog.data.local.RentEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rent_log_db"
        ).addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideLandlordDao(db: AppDatabase): LandlordDao = db.landlordDao()

    @Provides
    fun provideRentEntryDao(db: AppDatabase): RentEntryDao = db.rentEntryDao()
}
