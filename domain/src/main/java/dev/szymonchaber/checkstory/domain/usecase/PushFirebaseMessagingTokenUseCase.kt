package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.interactor.FirebaseMessagingInteractor
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import javax.inject.Inject

class PushFirebaseMessagingTokenUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseMessagingInteractor: FirebaseMessagingInteractor
) {

    suspend fun pushFirebaseMessagingToken(token: String) {
        if (!userRepository.getCurrentUser().isLoggedIn) {
            return
        }

        firebaseMessagingInteractor.pushFirebaseMessagingToken(token)
    }
}
