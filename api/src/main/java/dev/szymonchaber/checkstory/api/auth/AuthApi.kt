package dev.szymonchaber.checkstory.api.auth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.api.auth.model.ApiUser
import dev.szymonchaber.checkstory.api.auth.model.RegisterPayload
import dev.szymonchaber.checkstory.api.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.LoginError
import dev.szymonchaber.checkstory.domain.usecase.RegisterError
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

internal class AuthApi @Inject constructor(private val client: ConfiguredHttpClient) {

    suspend fun login(): Result<LoginError, User> {
        return try {
            if (Firebase.auth.currentUser == null) {
                return Result.error(LoginError.NetworkError(IllegalStateException("Firebase user not logged in")))
            }
            Result.success(
                client
                    .get("/auth/login")
                    .body<ApiUser>()
                    .toUser()
            )
        } catch (exception: Exception) {
            Result.error(LoginError.NetworkError(exception))
        }
    }

    suspend fun register(): Result<RegisterError, User> {
        return try {
            Result.success(
                client
                    .post("/auth/register") {
                        setBody(RegisterPayload())
                    }
                    .body<ApiUser>()
                    .toUser()
            )
        } catch (exception: Exception) {
            Result.error(RegisterError.NetworkError(exception))
        }
    }
}
