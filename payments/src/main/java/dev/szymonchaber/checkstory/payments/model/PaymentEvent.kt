package dev.szymonchaber.checkstory.payments.model

sealed interface PaymentEvent {

    object BuyClicked : PaymentEvent
}