package com.tukorea.bus.di

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.repository.TempRepositoryImpl
import com.tukorea.bus.domain.repository.TempRepository
import com.tukorea.bus.domain.usecase.GetTempListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TempModule {

    @Provides
    @Singleton
    fun provideTempRepository(api: ApiService): TempRepository =
        TempRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideGetTempListUseCase(repository: TempRepository): GetTempListUseCase =
        GetTempListUseCase(repository)
}