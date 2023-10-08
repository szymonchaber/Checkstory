package dev.szymonchaber.checkstory.payments.billing

import android.app.Activity
import arrow.core.Either
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import kotlinx.coroutines.flow.Flow

internal interface BillingInteractor : PlayPaymentRepository {

    val subscriptionPlans: Flow<Either<BillingError, SubscriptionPlans>?>

    suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails>

    fun startPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ): Flow<Either<PurchaseError, Purchase>>
}
