package dev.szymonchaber.checkstory.payments

import android.app.Activity
import arrow.core.Either
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import dev.szymonchaber.checkstory.payments.billing.BillingInteractor
import dev.szymonchaber.checkstory.payments.billing.PurchaseError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class PurchaseSubscriptionUseCase @Inject constructor(
    private val billingInteractor: BillingInteractor,
) {

    fun startPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ): Flow<Either<PurchaseError, Purchase>> {
        return billingInteractor.startPurchaseFlow(activity, productDetails, offerToken)
    }
}
