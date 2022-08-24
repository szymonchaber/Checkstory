package dev.szymonchaber.checkstory.payments

import android.app.Activity
import arrow.core.Either
import com.android.billingclient.api.ProductDetails

interface PurchaseSubscriptionUseCase {

    suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails>
    fun startPurchaseFlow(activity: Activity, productDetails: ProductDetails)
}
