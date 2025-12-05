package com.tukorea.bus.di

import com.tukorea.bus.data.repository.ReservationRepositoryImpl
import com.tukorea.bus.domain.repository.ReservationRepository
import com.tukorea.bus.domain.usecase.AddReservationUseCase
import com.tukorea.bus.domain.usecase.DeleteReservationUseCase
import com.tukorea.bus.domain.usecase.GetAllReservationsUseCase
import com.tukorea.bus.domain.usecase.GetNextReservationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReservationModule {
    
    @Provides
    @Singleton
    fun provideReservationRepository(
        repositoryImpl: ReservationRepositoryImpl
    ): ReservationRepository = repositoryImpl
    
    @Provides
    @Singleton
    fun provideGetNextReservationUseCase(
        repository: ReservationRepository
    ): GetNextReservationUseCase = GetNextReservationUseCase(repository)
    
    @Provides
    @Singleton
    fun provideGetAllReservationsUseCase(
        repository: ReservationRepository
    ): GetAllReservationsUseCase = GetAllReservationsUseCase(repository)
    
    @Provides
    @Singleton
    fun provideAddReservationUseCase(
        repository: ReservationRepository
    ): AddReservationUseCase = AddReservationUseCase(repository)
    
    @Provides
    @Singleton
    fun provideDeleteReservationUseCase(
        repository: ReservationRepository
    ): DeleteReservationUseCase = DeleteReservationUseCase(repository)
}

