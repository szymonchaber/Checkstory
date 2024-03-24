package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.interactor.FirebaseMessagingTokenProvider
import dev.szymonchaber.checkstory.domain.interactor.UserPaymentInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.tapSuccess
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val userRepository: UserRepository,
    private val synchronizer: Synchronizer,
    private val userPaymentInteractor: UserPaymentInteractor,
    private val paymentRepository: PlayPaymentRepository,
    private val firebaseTokenProvider: FirebaseMessagingTokenProvider,
    private val pushFirebaseTokenUseCase: PushFirebaseMessagingTokenUseCase,
) {

    suspend fun login(assignCurrentPayment: Boolean = false): Result<LoginError, User> {
        return authInteractor.login()
            .flatMapSuccess {
                if (assignCurrentPayment) {
                    assignExistingPurchaseToUserOrNull() ?: Result.success(it)
                } else {
                    Result.success(it)
                }
            }
            .tapSuccess {
                userRepository.storeCurrentUser(it)
                sendFirebaseTokenIgnoringResult()
                if (synchronizer.hasUnsynchronizedCommands()) {
                    synchronizer.scheduleCommandsSynchronization()
                } else {
                    synchronizer.scheduleDataFetch()
                }
            }
    }

    private suspend fun assignExistingPurchaseToUserOrNull(): Result<LoginError, User>? {
        return paymentRepository.getActiveSubscription()
            ?.let {
                userPaymentInteractor.assignPaymentToCurrentUser(it.token)
                    .handleError<LoginError> {
                        Timber.e(it.toString())
                        Result.success(Unit)
                    }
                    .flatMapSuccess {
                        authInteractor.login()
                            .mapError { loginError ->
                                when (loginError) {
                                    is LoginError.NetworkError -> LoginError.NetworkError(loginError.exception)
                                }
                            }
                    }
            }
    }

    private suspend fun sendFirebaseTokenIgnoringResult() {
        try {
            pushFirebaseTokenUseCase.pushFirebaseMessagingToken(firebaseTokenProvider.getFirebaseToken())
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }
}

sealed interface LoginError {

    data class NetworkError(val exception: Exception) : LoginError
}
