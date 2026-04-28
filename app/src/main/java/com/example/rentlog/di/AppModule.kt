package com.devchiradhi.rentlog.di

import android.content.Context
import androidx.room.Room
import com.devchiradhi.rentlog.data.local.AppDatabase
import com.devchiradhi.rentlog.data.local.LandlordDao
import com.devchiradhi.rentlog.data.local.RentEntryDao
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
        )
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
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
