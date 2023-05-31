package dev.szymonchaber.checkstory.api.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.api.ConfiguredHttpClient
import dev.szymonchaber.checkstory.api.auth.AuthInteractorImpl
import dev.szymonchaber.checkstory.api.payment.interactor.UserPaymentInteractorImpl
import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.interactor.UserPaymentInteractor
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
internal interface ApiModule {

    @Binds
    fun bindUserPaymentInteractor(userPaymentInteractorImpl: UserPaymentInteractorImpl): UserPaymentInteractor

    @Binds
    fun bindAuthInteractor(authInteractor: AuthInteractorImpl): AuthInteractor

    companion object {

        private const val TIME_OUT = 15_000

        private const val API_ENDPOINT = "http://10.0.2.2:8080"
//        private const val API_ENDPOINT = "http://159.203.187.86"

        @Provides
        fun provideHttpClient(): ConfiguredHttpClient {
            return HttpClient(Android) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Auth) {
                    bearer {
                        loadTokens {
                            getFirebaseIdToken(forceRefresh = false)
                        }
                        refreshTokens {
                            getFirebaseIdToken(forceRefresh = true)
                        }
                    }
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
                    url(API_ENDPOINT)
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                }
            }
        }

        private suspend fun getFirebaseIdToken(forceRefresh: Boolean): BearerTokens? {
            return Firebase.auth.currentUser
                ?.getIdToken(forceRefresh)
                ?.await()
                ?.token?.let {
                    BearerTokens(it, "")
                }
        }
    }
}