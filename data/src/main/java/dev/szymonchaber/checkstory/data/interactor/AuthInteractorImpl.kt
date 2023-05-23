package dev.szymonchaber.checkstory.data.interactor

import dev.szymonchaber.checkstory.data.api.auth.AuthApi
import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.LoginError
import javax.inject.Inject

internal class AuthInteractorImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthInteractor {

    override suspend fun login(): Result<LoginError, User> {
        return authApi.login()
    }
}
