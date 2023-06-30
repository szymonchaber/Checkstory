package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.interactor.UserPaymentInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.tapSuccess
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val userRepository: UserRepository,
    private val synchronizer: Synchronizer,
    private val userPaymentInteractor: UserPaymentInteractor,
    private val paymentRepository: PlayPaymentRepository
) {

    suspend fun register(): Result<RegisterError, User> {
        return authInteractor.register()
            .flatMapSuccess { user ->
                assignExistingPurchaseToUserOrNull() ?: Result.success(user)
            }
            .tapSuccess {
                userRepository.storeCurrentUser(it)
                synchronizer.scheduleDataFetch()
            }
    }

    private suspend fun assignExistingPurchaseToUserOrNull(): Result<RegisterError, User>? {
        return paymentRepository.getActiveSubscription()
            ?.let {
                userPaymentInteractor.assignPaymentToCurrentUser(it.token)
                    .handleError<RegisterError> {
                        Timber.e(it.toString())
                        Result.success(Unit)
                    }
                    .flatMapSuccess {
                        authInteractor.login()
                            .mapError { loginError ->
                                when (loginError) {
                                    is LoginError.NetworkError -> RegisterError.NetworkError(loginError.exception)
                                }
                            }
                    }
            }
    }
}

sealed interface RegisterError {

    data class NetworkError(val exception: Exception) : RegisterError
}
