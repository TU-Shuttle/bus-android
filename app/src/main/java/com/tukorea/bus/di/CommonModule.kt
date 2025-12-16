package com.tukorea.bus.di

import android.content.Context
import com.tukorea.bus.domain.usecase.GetDefaultLocationsUseCase
import com.tukorea.bus.ui.common.ErrorMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @Singleton
    fun provideGetDefaultLocationsUseCase(): GetDefaultLocationsUseCase =
        GetDefaultLocationsUseCase()

    @Provides
    @Singleton
    fun provideErrorMapper(
        @ApplicationContext context: Context
    ): ErrorMapper = ErrorMapper(context)
}

