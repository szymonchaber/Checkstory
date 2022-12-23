package dev.szymonchaber.checkstory.data.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
fimport dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.RemoteChecklistTemplateRepository
import dev.szymonchaber.checkstory.data.repository.TemplateCheckboxRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.TemplateReminderRepositoryImpl
import dev.szymonchaber.checkstory.data.synchronization.SynchronizerImpl
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import javax.inject.Named
import javax.inject.Singleton
import dev.szymonchaber.checkstory.domain.usecase.Synchronizer
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindChecklistRepository(repository: ChecklistRepositoryImpl): ChecklistRepository

//    @Binds
//    fun bindChecklistTemplateRepository(repository: LocalChecklistTemplateRepository): ChecklistTemplateRepository

    @Binds
    fun bindChecklistTemplateRepository(repository: RemoteChecklistTemplateRepository): ChecklistTemplateRepository

    @Binds
    fun bindTemplateCheckboxRepository(repository: TemplateCheckboxRepositoryImpl): TemplateCheckboxRepository

    @Binds
    fun bindTemplateReminderRepository(repository: TemplateReminderRepositoryImpl): TemplateReminderRepository

    @Binds
    fun bindSynchronizer(synchronizer: SynchronizerImpl): Synchronizer

    companion object {

        @Provides
        @Named("onboardingPreferences")
        @Singleton
        fun provideOnboardingPreferences(application: Application): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create {
                application.preferencesDataStoreFile("onboarding_preferences")
            }
        }

        private const val TIME_OUT = 60_000

        @Provides
        fun provideHttpClient(): HttpClient {
            return HttpClient(Android) {
                install(JsonFeature) {
                    serializer = KotlinxSerializer(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                engine {
                    connectTimeout = TIME_OUT
                    socketTimeout = TIME_OUT
                }

                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Timber.v(message)
                        }
                    }
                    level = LogLevel.ALL
                }

                install(ResponseObserver) {
                    onResponse { response ->
                        Timber.d("HTTP status: ${response.status.value}")
                    }
                }

                install(DefaultRequest) {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                }
            }
        }
    }
}
