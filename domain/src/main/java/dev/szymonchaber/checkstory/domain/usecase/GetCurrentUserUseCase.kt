package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.domain.repository.SubscriptionStatus
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val playPaymentRepository: PlayPaymentRepository
) {

    fun getCurrentUserFlow(): Flow<User> {
        return userRepository.getCurrentUserFlow()
            .combine(playPaymentRepository.subscriptionStatusFlow) { user, activeSubscription ->
                when (user) {
                    is User.Guest -> {
                        user.copy(deviceHasLocalPayment = activeSubscription is SubscriptionStatus.Active)
                    }
                    is User.LoggedIn -> user
                }
            }
    }

    suspend fun getCurrentUser(): User {
        return userRepository.getCurrentUser()
    }
}
