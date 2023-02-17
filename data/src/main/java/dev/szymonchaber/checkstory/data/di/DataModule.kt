package dev.szymonchaber.checkstory.data.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.ChecklistTemplateRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.TemplateCheckboxRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.TemplateReminderRepositoryImpl
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindChecklistRepository(repository: ChecklistRepositoryImpl): ChecklistRepository

    @Binds
    fun bindChecklistTemplateRepository(repository: ChecklistTemplateRepositoryImpl): ChecklistTemplateRepository

    @Binds
    fun bindTemplateCheckboxRepository(repository: TemplateCheckboxRepositoryImpl): TemplateCheckboxRepository

    @Binds
    fun bindTemplateReminderRepository(repository: TemplateReminderRepositoryImpl): TemplateReminderRepository

    companion object {

        @Provides
        @Named("onboardingPreferences")
        @Singleton
        fun provideOnboardingPreferences(application: Application): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create {
                application.preferencesDataStoreFile("onboarding_preferences")
            }
        }
    }
}
