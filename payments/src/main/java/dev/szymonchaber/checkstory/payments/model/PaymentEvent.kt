package dev.szymonchaber.checkstory.payments.model

import android.app.Activity
import arrow.core.Either
import com.android.billingclient.api.Purchase
import dev.szymonchaber.checkstory.payments.PurchaseError

sealed interface PaymentEvent {

    object LoadSubscriptionPlans : PaymentEvent

    data class BuyClicked(val activity: Activity) : PaymentEvent // passing activity is cringe

    data class NewPurchaseResult(val paymentResult: Either<PurchaseError, Purchase>) : PaymentEvent
}