package dev.szymonchaber.checkstory.api.auth

import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.DeleteAccountError
import dev.szymonchaber.checkstory.domain.usecase.LoginError
import dev.szymonchaber.checkstory.domain.usecase.RegisterError
import javax.inject.Inject

internal class AuthInteractorImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthInteractor {

    override suspend fun login(): Result<LoginError, User> {
        return authApi.login()
    }

    override suspend fun register(): Result<RegisterError, User> {
        return authApi.register()
    }

    override suspend fun deleteAccount(): Result<DeleteAccountError, Unit> {
        return authApi.deleteAccount()
    }
}
