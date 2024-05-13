package dev.szymonchaber.checkstory.domain.interactor

import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.DeleteAccountError
import dev.szymonchaber.checkstory.domain.usecase.LoginError
import dev.szymonchaber.checkstory.domain.usecase.RegisterError

interface AuthInteractor {

    suspend fun login(): Result<LoginError, User.LoggedIn>

    suspend fun register(): Result<RegisterError, User.LoggedIn>

    suspend fun deleteAccount(): Result<DeleteAccountError, Unit>
}
