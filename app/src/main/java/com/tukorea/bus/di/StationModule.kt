package com.tukorea.bus.di

import com.tukorea.bus.data.repository.StationRepositoryImpl
import com.tukorea.bus.domain.repository.StationRepository
import com.tukorea.bus.domain.usecase.GetAllRoutesUseCase
import com.tukorea.bus.domain.usecase.GetStationsByRouteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StationModule {
    
    @Provides
    @Singleton
    fun provideStationRepository(
        repositoryImpl: StationRepositoryImpl
    ): StationRepository = repositoryImpl
    
    @Provides
    @Singleton
    fun provideGetStationsByRouteUseCase(
        repository: StationRepository
    ): GetStationsByRouteUseCase = GetStationsByRouteUseCase(repository)
    
    @Provides
    @Singleton
    fun provideGetAllRoutesUseCase(
        repository: StationRepository
    ): GetAllRoutesUseCase = GetAllRoutesUseCase(repository)
}

