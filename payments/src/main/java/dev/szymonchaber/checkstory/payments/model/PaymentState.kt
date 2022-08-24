package dev.szymonchaber.checkstory.payments.model

data class PaymentState(
    val isLoading: Boolean = false,
    val result: String = "Idle"
) {

    companion object {

        val initial = PaymentState()
    }
}