package dev.szymonchaber.checkstory.payments.model

@Suppress("CanSealedSubClassBeObject")
internal sealed interface PaymentEffect {

    class PaymentError : PaymentEffect
    class NavigateToPaymentSuccess : PaymentEffect
    class ExitPaymentScreen : PaymentEffect
}
