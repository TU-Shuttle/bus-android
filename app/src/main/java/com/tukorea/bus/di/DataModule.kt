package com.tukorea.bus.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tukorea.bus.data.repository.LiveBusRepositoryImpl
import com.tukorea.bus.data.repository.SystemTimeProvider
import com.tukorea.bus.data.repository.TimetableRepositoryImpl
import com.tukorea.bus.domain.repository.LiveBusRepository
import com.tukorea.bus.domain.repository.TimeProvider
import com.tukorea.bus.domain.repository.TimetableRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.shuttleDataStore: DataStore<Preferences> by preferencesDataStore(name = "shuttle")

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindTimetableRepository(impl: TimetableRepositoryImpl): TimetableRepository

    @Binds
    abstract fun bindLiveBusRepository(impl: LiveBusRepositoryImpl): LiveBusRepository

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.shuttleDataStore
    }
}
