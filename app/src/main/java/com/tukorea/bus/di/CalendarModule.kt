package com.tukorea.bus.di

import com.tukorea.bus.data.repository.CalendarRepositoryImpl
import com.tukorea.bus.domain.repository.CalendarRepository
import com.tukorea.bus.domain.usecase.GetDaysUseCase
import com.tukorea.bus.domain.usecase.GetLocationsUseCase
import com.tukorea.bus.domain.usecase.GetTimesByRouteUseCase
import com.tukorea.bus.domain.usecase.SortDaysUseCase
import com.tukorea.bus.domain.usecase.ValidateReservationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalendarModule {
    
    @Provides
    @Singleton
    fun provideCalendarRepository(
        repositoryImpl: CalendarRepositoryImpl
    ): CalendarRepository = repositoryImpl

    @Provides
    @Singleton
    fun provideGetDaysUseCase(
        repository: CalendarRepository
    ): GetDaysUseCase = GetDaysUseCase(repository)

    @Provides
    @Singleton
    fun provideGetLocationsUseCase(
        repository: CalendarRepository
    ): GetLocationsUseCase = GetLocationsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTimesByRouteUseCase(
        repository: CalendarRepository
    ): GetTimesByRouteUseCase = GetTimesByRouteUseCase(repository)

    @Provides
    @Singleton
    fun provideValidateReservationUseCase(): ValidateReservationUseCase = ValidateReservationUseCase()

    @Provides
    @Singleton
    fun provideSortDaysUseCase(): SortDaysUseCase = SortDaysUseCase()
}

