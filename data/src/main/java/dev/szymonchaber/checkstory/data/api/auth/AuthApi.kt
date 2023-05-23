package dev.szymonchaber.checkstory.data.api.auth

import dev.szymonchaber.checkstory.data.api.auth.dto.ApiUser
import dev.szymonchaber.checkstory.data.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.LoginError
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

internal class AuthApi @Inject constructor(private val client: ConfiguredHttpClient) {

    suspend fun login(): Result<LoginError, User> {
        return try {
            Result.success(
                client
                    .get("/auth/login")
                    .body<ApiUser>()
                    .toUser()
            )
        } catch (_: Exception) {
            Result.error(LoginError.NetworkError)
        }
    }
}
