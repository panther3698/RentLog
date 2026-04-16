package com.example.rentlog.di

import android.content.Context
import androidx.room.Room
import com.example.rentlog.data.local.AppDatabase
import com.example.rentlog.data.local.LandlordDao
import com.example.rentlog.data.local.RentEntryDao
import com.example.rentlog.data.local.RentLogDao
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
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRentLogDao(db: AppDatabase): RentLogDao {
        return db.rentLogDao()
    }

    @Provides
    fun provideLandlordDao(db: AppDatabase): LandlordDao {
        return db.landlordDao()
    }

    @Provides
    fun provideRentEntryDao(db: AppDatabase): RentEntryDao {
        return db.rentEntryDao()
    }
}
