package dev.szymonchaber.checkstory.domain.interactor

import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.usecase.LoginError

interface AuthInteractor {

    suspend fun login(): Result<LoginError, User>
}
