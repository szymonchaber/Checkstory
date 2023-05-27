package dev.szymonchaber.checkstory.payments

import android.app.Activity
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import dev.szymonchaber.checkstory.domain.model.fold
import dev.szymonchaber.checkstory.domain.model.payment.PurchaseToken
import dev.szymonchaber.checkstory.domain.usecase.AssignPaymentToUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PurchaseSubscriptionUseCase @Inject constructor(
    private val billingInteractor: BillingInteractor,
    private val assignPaymentToUserUseCase: AssignPaymentToUserUseCase
) {

    fun startPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ): Flow<Either<PurchaseError, Purchase>> {
        return billingInteractor.startPurchaseFlow(activity, productDetails, offerToken)
            .map {
                it.flatMap { purchase ->
                    assignPaymentToUserUseCase.assignPurchaseTokenToUser(PurchaseToken(purchase.purchaseToken))
                        .fold(
                            mapError = {
                                PurchaseError.CheckstoryBackendConnectionError(it).left()
                            },
                            mapSuccess = {
                                purchase.right()
                            }
                        )
                }
            }
    }
}
