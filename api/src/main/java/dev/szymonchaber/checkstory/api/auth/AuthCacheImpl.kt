package dev.szymonchaber.checkstory.api.auth

import dev.szymonchaber.checkstory.api.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.interactor.AuthCache
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import javax.inject.Inject

internal class AuthCacheImpl @Inject constructor(val client: ConfiguredHttpClient) : AuthCache {

    override suspend fun clearAuthToken() {
        client.plugin(Auth)
            .providers
            .filterIsInstance<BearerAuthProvider>()
            .firstOrNull()
            ?.clearToken()
    }
}
