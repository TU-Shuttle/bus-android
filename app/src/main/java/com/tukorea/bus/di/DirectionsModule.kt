package com.tukorea.bus.di

import com.tukorea.bus.data.repository.DirectionsRepositoryImpl
import com.tukorea.bus.domain.repository.DirectionsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DirectionsModule {

    @Provides
    @Singleton
    fun provideDirectionsRepository(
        repositoryImpl: DirectionsRepositoryImpl
    ): DirectionsRepository = repositoryImpl
}
