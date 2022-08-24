package dev.szymonchaber.checkstory.payments.model

data class PaymentState(
    val isLoading: Boolean = false
) {

    companion object {

        val initial = PaymentState()
    }
}