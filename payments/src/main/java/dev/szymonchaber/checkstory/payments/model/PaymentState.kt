package dev.szymonchaber.checkstory.payments.model

import dev.szymonchaber.checkstory.payments.PlanDuration
import dev.szymonchaber.checkstory.payments.PlanDurationUnit
import dev.szymonchaber.checkstory.payments.SubscriptionPlan

data class PaymentState(
    val isLoading: Boolean = false,
    val result: String = "Idle",
    val plans: List<SubscriptionPlan> = listOf(
        SubscriptionPlan(PlanDuration(1, PlanDurationUnit.MONTH), "$8.99", "$8.99/mo"),
        SubscriptionPlan(PlanDuration(12, PlanDurationUnit.MONTH), "$85,99", "$6.99/mo"),
        SubscriptionPlan(PlanDuration(3, PlanDurationUnit.MONTH), "$25,99", "$8.69/mo"),
    ),
    val selectedPlan: SubscriptionPlan? = SubscriptionPlan(
        PlanDuration(1, PlanDurationUnit.MONTH),
        "$8.99",
        "$8.99/mo"
    ),
    val paymentLoadingState: PaymentLoadingState
) {

    companion object {

        val initial = PaymentState(paymentLoadingState = PaymentLoadingState.Loading)
    }

    sealed interface PaymentLoadingState {

        data class Success(
            val result: String = "Idle",
            val plans: List<SubscriptionPlan> = listOf(
                SubscriptionPlan(PlanDuration(1, PlanDurationUnit.MONTH), "$8.99", "$8.99/mo"),
                SubscriptionPlan(PlanDuration(12, PlanDurationUnit.MONTH), "$85,99", "$6.99/mo"),
                SubscriptionPlan(PlanDuration(3, PlanDurationUnit.MONTH), "$25,99", "$8.69/mo"),
            ),
            val selectedPlan: SubscriptionPlan? = SubscriptionPlan(
                PlanDuration(1, PlanDurationUnit.MONTH),
                "$8.99",
                "$8.99/mo"
            )
        ) : PaymentLoadingState

        object Loading : PaymentLoadingState
    }
}