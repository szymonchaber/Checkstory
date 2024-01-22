package dev.szymonchaber.checkstory.payments.billing

import dev.szymonchaber.checkstory.domain.interactor.AssignPaymentError
import dev.szymonchaber.checkstory.domain.usecase.RegisterError

internal sealed interface PurchaseError {

    data object UserCancelled : PurchaseError

    data class Unhandled(val billingResultResponseCode: Int, val billingResultDebugMessage: String) : PurchaseError

    data class PurchaseListHasIncorrectSize(val actualSize: Int) : PurchaseError

    object PurchaseListIsNullDespiteStatusOK : PurchaseError

    data class ConnectionError(val debugMessage: String) : PurchaseError

    data class CheckstoryBackendConnectionError(val exception: AssignPaymentError) : PurchaseError

    data class CheckstoryAuthenticationError(val exception: RegisterError) : PurchaseError

    data object AlreadySubscribed : PurchaseError
}
