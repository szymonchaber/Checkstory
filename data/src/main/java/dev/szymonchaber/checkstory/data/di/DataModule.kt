package dev.szymonchaber.checkstory.data.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.data.repository.*
import dev.szymonchaber.checkstory.data.synchronization.SynchronizerImpl
import dev.szymonchaber.checkstory.domain.repository.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindChecklistRepository(repository: ChecklistRepositoryImpl): ChecklistRepository

    @Binds
    fun bindTemplateRepository(repository: TemplateRepositoryImpl): TemplateRepository

    @Binds
    fun bindTemplateCheckboxRepository(repository: TemplateCheckboxRepositoryImpl): TemplateCheckboxRepository

    @Binds
    fun bindTemplateReminderRepository(repository: TemplateReminderRepositoryImpl): TemplateReminderRepository

    @Binds
    fun bindSynchronizer(synchronizer: SynchronizerImpl): Synchronizer

    @Binds
    fun bindUserRepository(userRepository: UserRepositoryImpl): UserRepository

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

        @Provides
        fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
            return FirebaseCrashlytics.getInstance()
        }
    }
}
