package com.tukorea.bus.di

import com.tukorea.bus.data.repository.NotificationRepositoryImpl
import com.tukorea.bus.domain.repository.NotificationRepository
import com.tukorea.bus.domain.usecase.GetNotificationsUseCase
import com.tukorea.bus.domain.usecase.MarkAllNotificationsAsReadUseCase
import com.tukorea.bus.domain.usecase.MarkNotificationAsReadUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(
        repositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository = repositoryImpl

    @Provides
    @Singleton
    fun provideGetNotificationsUseCase(
        repository: NotificationRepository
    ): GetNotificationsUseCase = GetNotificationsUseCase(repository)

    @Provides
    @Singleton
    fun provideMarkNotificationAsReadUseCase(
        repository: NotificationRepository
    ): MarkNotificationAsReadUseCase = MarkNotificationAsReadUseCase(repository)

    @Provides
    @Singleton
    fun provideMarkAllNotificationsAsReadUseCase(
        repository: NotificationRepository
    ): MarkAllNotificationsAsReadUseCase = MarkAllNotificationsAsReadUseCase(repository)
}

