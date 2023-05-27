package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.domain.repository.SubscriptionStatus
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckForUnassignedPaymentUseCase @Inject constructor(
    private val userUseCase: GetCurrentUserUseCase,
    private val playPaymentRepository: PlayPaymentRepository
) {

    suspend fun isUnassignedPaymentPresent(): Boolean {
        return isUserLoggedOut() && isActivePurchasePresent()
    }

    private suspend fun isUserLoggedOut(): Boolean {
        return userUseCase.getCurrentUser().isLoggedIn.not()
    }

    private suspend fun isActivePurchasePresent(): Boolean {
        return playPaymentRepository.subscriptionStatusFlow.filterNotNull().first() is SubscriptionStatus.Active
    }
}
