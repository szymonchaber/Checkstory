package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val logoutUseCase: LogoutUseCase
) {

    suspend fun deleteAccount(): Result<DeleteAccountError, Unit> {
        return authInteractor.deleteAccount().mapSuccess {
            logoutUseCase.logoutIgnoringUnsynchronizedData()
        }
    }
}

sealed interface DeleteAccountError {

    data class NetworkError(val exception: Exception) : DeleteAccountError
}
