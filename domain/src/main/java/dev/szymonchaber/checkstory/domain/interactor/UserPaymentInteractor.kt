package dev.szymonchaber.checkstory.domain.interactor

import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.payment.PurchaseToken

interface UserPaymentInteractor {

    suspend fun assignPaymentToCurrentUser(purchaseToken: PurchaseToken): Result<AssignPaymentError, Unit>
}

sealed interface AssignPaymentError {

    object NetworkError : AssignPaymentError

    object PurchaseTokenAssignedToAnotherUser : AssignPaymentError
}
