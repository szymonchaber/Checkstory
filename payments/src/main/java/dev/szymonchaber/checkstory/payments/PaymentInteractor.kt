package dev.szymonchaber.checkstory.payments

import android.app.Activity
import arrow.core.Either
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.Flow

interface PaymentInteractor {

    val purchaseEvents: Flow<Either<PurchaseError, Purchase>>

    suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails>

    fun startPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String)

    suspend fun getPaymentPlans(): Either<BillingError, SubscriptionPlans>
}
