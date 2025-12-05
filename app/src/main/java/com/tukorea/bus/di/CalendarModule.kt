package com.tukorea.bus.di

import com.tukorea.bus.data.repository.CalendarRepositoryImpl
import com.tukorea.bus.domain.repository.CalendarRepository
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
}

