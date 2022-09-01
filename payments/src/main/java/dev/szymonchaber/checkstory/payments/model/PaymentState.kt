package dev.szymonchaber.checkstory.payments.model

import dev.szymonchaber.checkstory.payments.SubscriptionPlan
import dev.szymonchaber.checkstory.payments.SubscriptionPlans

data class PaymentState<LoadingState : PaymentState.PaymentLoadingState>(
    val paymentLoadingState: LoadingState
) {

    companion object {

        val initial = PaymentState(paymentLoadingState = PaymentLoadingState.Loading)
    }

    sealed interface PaymentLoadingState {

        data class Success(
            val plans: SubscriptionPlans,
            val selectedPlan: SubscriptionPlan,
            val paymentInProgress: Boolean
        ) : PaymentLoadingState

        object LoadingError : PaymentLoadingState

        object Loading : PaymentLoadingState
    }
}
