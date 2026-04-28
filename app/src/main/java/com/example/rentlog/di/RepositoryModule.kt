package com.devchiradhi.rentlog.di

import com.devchiradhi.rentlog.data.repository.LandlordRepositoryImpl
import com.devchiradhi.rentlog.data.repository.RentEntryRepositoryImpl
import com.devchiradhi.rentlog.domain.repository.LandlordRepository
import com.devchiradhi.rentlog.domain.repository.RentEntryRepository
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
    abstract fun bindLandlordRepository(
        landlordRepositoryImpl: LandlordRepositoryImpl
    ): LandlordRepository

    @Binds
    @Singleton
    abstract fun bindRentEntryRepository(
        rentEntryRepositoryImpl: RentEntryRepositoryImpl
    ): RentEntryRepository
}
