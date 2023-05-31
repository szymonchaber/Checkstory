package dev.szymonchaber.checkstory.payments

sealed interface BillingError {

    object BillingNotSupported : BillingError

    data class Unhandled(val billingResultResponseCode: Int, val billingResultDebugMessage: String) : BillingError

    data class IncorrectSkuProvided(val sku: String) : BillingError

    data class NoProductsMatch(val sku: String) : BillingError

    data class ConnectionError(val debugMessage: String) : BillingError

    object BillingClientConnectionError : BillingError
}
