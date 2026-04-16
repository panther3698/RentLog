package com.example.rentlog.di

import com.example.rentlog.data.repository.RentLogRepositoryImpl
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
}
