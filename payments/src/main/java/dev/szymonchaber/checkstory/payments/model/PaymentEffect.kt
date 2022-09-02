package dev.szymonchaber.checkstory.payments.model

@Suppress("CanSealedSubClassBeObject")
sealed interface PaymentEffect {

    class PaymentError : PaymentEffect
    class ExitPaymentScreen : PaymentEffect
}
