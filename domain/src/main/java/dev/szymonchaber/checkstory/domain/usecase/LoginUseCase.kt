package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.interactor.AuthInteractor
import dev.szymonchaber.checkstory.domain.interactor.FirebaseMessagingTokenProvider
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.tapSuccess
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val userRepository: UserRepository,
    private val synchronizer: Synchronizer,
    private val firebaseTokenProvider: FirebaseMessagingTokenProvider,
    private val pushFirebaseTokenUseCase: PushFirebaseMessagingTokenUseCase
) {

    suspend fun login(): Result<LoginError, User> {
        return authInteractor.login()
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
