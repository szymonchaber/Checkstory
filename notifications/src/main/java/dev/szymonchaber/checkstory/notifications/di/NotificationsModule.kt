package dev.szymonchaber.checkstory.notifications.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.domain.interactor.FirebaseMessagingTokenProvider
import dev.szymonchaber.checkstory.notifications.FirebaseMessagingTokenProviderImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface NotificationsModule {

    @Binds
    fun bindFirebaseMessagingTokenProvider(repository: FirebaseMessagingTokenProviderImpl): FirebaseMessagingTokenProvider
}
