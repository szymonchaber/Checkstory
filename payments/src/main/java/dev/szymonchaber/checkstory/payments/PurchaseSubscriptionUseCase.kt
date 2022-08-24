package dev.szymonchaber.checkstory.payments

import android.app.Activity
import arrow.core.Either
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.Flow

interface PurchaseSubscriptionUseCase {

    suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails>

    fun startPurchaseFlow(activity: Activity, productDetails: ProductDetails)

    val purchaseEvents: Flow<Either<PurchaseError, Purchase>>
}
