package com.example.rentlog.di

import com.example.rentlog.data.repository.LandlordRepositoryImpl
import com.example.rentlog.data.repository.RentEntryRepositoryImpl
import com.example.rentlog.data.repository.RentLogRepositoryImpl
import com.example.rentlog.domain.repository.LandlordRepository
import com.example.rentlog.domain.repository.RentEntryRepository
import com.example.rentlog.domain.repository.RentLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRentLogRepository(
        rentLogRepositoryImpl: RentLogRepositoryImpl
    ): RentLogRepository

    @Binds
    @Singleton
    abstract fun bindLandlordRepository(
        landlordRepositoryImpl: LandlordRepositoryImpl
    ): LandlordRepository

    @Binds
    @Singleton
    abstract fun bindRentEntryRepository(
        rentEntryRepositoryImpl: RentEntryRepositoryImpl
    ): RentEntryRepository
}
