package dev.szymonchaber.checkstory.payments.model

import dev.szymonchaber.checkstory.payments.billing.SubscriptionPlan
import dev.szymonchaber.checkstory.payments.billing.SubscriptionPlans

data class PaymentState<LoadingState : PaymentState.PaymentLoadingState>(
    val paymentLoadingState: LoadingState
) {

    companion object {

        val initial = PaymentState(paymentLoadingState = PaymentLoadingState.Loading)
    }

    sealed interface PaymentLoadingState {

        object Paid : PaymentLoadingState

        data class Success(
            val plans: SubscriptionPlans,
            val selectedPlan: SubscriptionPlan,
            val paymentInProgress: Boolean
        ) : PaymentLoadingState

        object LoadingError : PaymentLoadingState

        object Loading : PaymentLoadingState

        companion object {

            fun success(plans: SubscriptionPlans): Success {
                return Success(
                    plans,
                    selectedPlan = plans.yearly,
                    paymentInProgress = false
                )
            }
        }
    }
}
