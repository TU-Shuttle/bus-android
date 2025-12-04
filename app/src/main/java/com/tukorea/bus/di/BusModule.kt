package com.tukorea.bus.di

import com.tukorea.bus.data.repository.BusRepositoryImpl
import com.tukorea.bus.domain.repository.BusRepository
import com.tukorea.bus.domain.usecase.GetAvailableBusesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BusModule {
    
    @Provides
    @Singleton
    fun provideBusRepository(
        repositoryImpl: BusRepositoryImpl
    ): BusRepository = repositoryImpl
    
    @Provides
    @Singleton
    fun provideGetAvailableBusesUseCase(
        repository: BusRepository
    ): GetAvailableBusesUseCase = GetAvailableBusesUseCase(repository)
}

