package dev.szymonchaber.checkstory.payments.model

import dev.szymonchaber.checkstory.payments.SubscriptionPlan

data class PaymentState(
    val paymentLoadingState: PaymentLoadingState
) {

    companion object {

        val initial = PaymentState(paymentLoadingState = PaymentLoadingState.Loading)
    }

    sealed interface PaymentLoadingState {

        data class Success(
            val result: String,
            val plans: List<SubscriptionPlan>,
            val selectedPlan: SubscriptionPlan?
        ) : PaymentLoadingState

        object Error : PaymentLoadingState

        object Loading : PaymentLoadingState
    }
}