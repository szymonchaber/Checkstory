package dev.szymonchaber.checkstory.payments

sealed interface PurchaseError {

    object UserCancelled : PurchaseError

    data class Unhandled(val billingResultResponseCode: Int, val billingResultDebugMessage: String) : PurchaseError

    data class PurchaseListHasIncorrectSize(val actualSize: Int) : PurchaseError

    object PurchaseListIsNullDespiteStatusOK : PurchaseError

    data class ConnectionError(val debugMessage: String) : PurchaseError

    data class CheckstoryBackendConnectionError(val exception: Exception) : PurchaseError

    object AlreadySubscribed : PurchaseError
}
