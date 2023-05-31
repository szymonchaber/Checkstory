package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.payment.ActiveSubscription
import kotlinx.coroutines.flow.Flow

interface PlayPaymentRepository {

    suspend fun getActiveSubscription(): ActiveSubscription?

    val subscriptionStatusFlow: Flow<SubscriptionStatus?>
}

sealed interface SubscriptionStatus {

    data class Active(val activeSubscription: ActiveSubscription) : SubscriptionStatus

    object Inactive : SubscriptionStatus
}
