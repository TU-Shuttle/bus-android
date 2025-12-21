package com.tukorea.bus.di

import com.tukorea.bus.data.repository.BusRepositoryImpl
import com.tukorea.bus.data.repository.BusStopRepositoryImpl
import com.tukorea.bus.domain.repository.BusRepository
import com.tukorea.bus.domain.repository.BusStopRepository
import com.tukorea.bus.domain.usecase.GetAvailableBusesUseCase
import com.tukorea.bus.domain.usecase.GetAllBusStopsUseCase
import com.tukorea.bus.domain.usecase.GetBusesForStopUseCase
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
    fun provideBusStopRepository(
        repositoryImpl: BusStopRepositoryImpl
    ): BusStopRepository = repositoryImpl
    
    @Provides
    @Singleton
    fun provideGetAvailableBusesUseCase(
        repository: BusRepository
    ): GetAvailableBusesUseCase = GetAvailableBusesUseCase(repository)
    
    @Provides
    @Singleton
    fun provideGetAllBusStopsUseCase(
        repository: BusStopRepository
    ): GetAllBusStopsUseCase = GetAllBusStopsUseCase(repository)
    
    @Provides
    @Singleton
    fun provideGetBusesForStopUseCase(
        repository: BusStopRepository
    ): GetBusesForStopUseCase = GetBusesForStopUseCase(repository)
}

