package dev.szymonchaber.checkstory.api.auth

import com.google.firebase.auth.FirebaseAuth
import dev.szymonchaber.checkstory.api.auth.model.ApiUser
import dev.szymonchaber.checkstory.api.auth.model.RegisterPayload
import dev.szymonchaber.checkstory.api.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.DeleteAccountError
import dev.szymonchaber.checkstory.domain.usecase.LoginError
import dev.szymonchaber.checkstory.domain.usecase.RegisterError
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

internal class AuthApi @Inject constructor(
    private val client: ConfiguredHttpClient,
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun login(): Result<LoginError, User.LoggedIn> {
        return try {
            if (firebaseAuth.currentUser == null) {
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

    suspend fun register(): Result<RegisterError, User.LoggedIn> {
        return try {
            if (firebaseAuth.currentUser == null) {
                return Result.error(RegisterError.NetworkError(IllegalStateException("Firebase user not logged in")))
            }
            Result.success(
                client
                    .post("/auth/register") {
                        setBody(
                            RegisterPayload(
                                firebaseAuth.currentUser?.email
                            )
                        )
                    }
                    .body<ApiUser>()
                    .toUser()
            )
        } catch (exception: Exception) {
            Result.error(RegisterError.NetworkError(exception))
        }
    }

    suspend fun deleteAccount(): Result<DeleteAccountError, Unit> {
        return try {
            client.post("/auth/delete")
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.error(DeleteAccountError.NetworkError(exception))
        }
    }
}
