package com.tukorea.bus.di

import com.tukorea.bus.data.repository.RouteScheduleRepositoryImpl
import com.tukorea.bus.domain.repository.RouteScheduleRepository
import com.tukorea.bus.domain.usecase.GetRouteSchedulesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RouteScheduleModule {

    @Provides
    @Singleton
    fun provideRouteScheduleRepository(
        impl: RouteScheduleRepositoryImpl
    ): RouteScheduleRepository = impl

    @Provides
    @Singleton
    fun provideGetRouteSchedulesUseCase(
        repository: RouteScheduleRepository
    ): GetRouteSchedulesUseCase = GetRouteSchedulesUseCase(repository)
}


