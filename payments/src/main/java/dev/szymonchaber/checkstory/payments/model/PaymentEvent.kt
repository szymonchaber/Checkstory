package dev.szymonchaber.checkstory.payments.model

import android.app.Activity
import dev.szymonchaber.checkstory.payments.billing.SubscriptionPlan

sealed interface PaymentEvent {

    object LoadSubscriptionPlans : PaymentEvent

    data class BuyClicked(val activity: Activity) : PaymentEvent // passing activity is cringe

    data class PlanSelected(val subscriptionPlan: SubscriptionPlan) : PaymentEvent

    object ContinueClicked : PaymentEvent
}
