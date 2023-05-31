package dev.szymonchaber.checkstory.data.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.data.api.payment.UserPaymentInteractorImpl
import dev.szymonchaber.checkstory.data.interactor.AuthInteractorImpl
import dev.szymonchaber.checkstory.data.repository.*
import dev.szymonchaber.checkstory.data.synchronization.SynchronizerImpl
import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.interactor.UserPaymentInteractor
import dev.szymonchaber.checkstory.domain.repository.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindChecklistRepository(repository: ChecklistRepositoryImpl): ChecklistRepository

    @Binds
    fun bindChecklistTemplateRepository(repository: ChecklistTemplateRepositoryImpl): ChecklistTemplateRepository

    @Binds
    fun bindTemplateCheckboxRepository(repository: TemplateCheckboxRepositoryImpl): TemplateCheckboxRepository

    @Binds
    fun bindTemplateReminderRepository(repository: TemplateReminderRepositoryImpl): TemplateReminderRepository

    @Binds
    fun bindSynchronizer(synchronizer: SynchronizerImpl): Synchronizer

    @Binds
    fun bindAuthInteractor(authInteractor: AuthInteractorImpl): AuthInteractor

    @Binds
    fun bindUserRepository(userRepository: UserRepositoryImpl): UserRepository

    @Binds
    fun bindUserPaymentInteractor(userPaymentInteractorImpl: UserPaymentInteractorImpl): UserPaymentInteractor

    companion object {

        @Provides
        @Named("onboardingPreferences")
        @Singleton
        fun provideOnboardingPreferences(application: Application): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create {
                application.preferencesDataStoreFile("onboarding_preferences")
            }
        }

        @Provides
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager
                .getInstance(context)
        }
    }
}
