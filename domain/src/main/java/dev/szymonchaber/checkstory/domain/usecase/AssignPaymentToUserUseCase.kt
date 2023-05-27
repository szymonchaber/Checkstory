package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.interactor.AssignPaymentError
import dev.szymonchaber.checkstory.domain.interactor.UserPaymentInteractor
import dev.szymonchaber.checkstory.domain.model.Result
import dev.szymonchaber.checkstory.domain.model.payment.PurchaseToken
import dev.szymonchaber.checkstory.domain.model.tapSuccess
import javax.inject.Inject

class AssignPaymentToUserUseCase @Inject constructor(
    private val interactor: UserPaymentInteractor,
    private val loginUseCase: LoginUseCase
) {

    suspend fun assignPurchaseTokenToUser(purchaseToken: PurchaseToken): Result<AssignPaymentError, Unit> {
        return interactor.assignPaymentToCurrentUser(purchaseToken)
            .tapSuccess {
                loginUseCase.login()
            }
    }
}
