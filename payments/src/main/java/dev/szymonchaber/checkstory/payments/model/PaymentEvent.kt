package dev.szymonchaber.checkstory.payments.model

import android.app.Activity
import dev.szymonchaber.checkstory.payments.billing.SubscriptionPlan

internal sealed interface PaymentEvent {

    data object LoadSubscriptionPlans : PaymentEvent
    data class RegistrationSuccess(val activity: Activity) : PaymentEvent
    data object RegistrationCancelled : PaymentEvent

    data class BuyClicked(val activity: Activity) : PaymentEvent // passing activity is cringe

    data class PlanSelected(val subscriptionPlan: SubscriptionPlan) : PaymentEvent
}
