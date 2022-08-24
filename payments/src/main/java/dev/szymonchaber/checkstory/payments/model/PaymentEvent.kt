package dev.szymonchaber.checkstory.payments.model

import android.app.Activity

sealed interface PaymentEvent {

    data class BuyClicked(val activity: Activity) : PaymentEvent // passing activity is cringe
}