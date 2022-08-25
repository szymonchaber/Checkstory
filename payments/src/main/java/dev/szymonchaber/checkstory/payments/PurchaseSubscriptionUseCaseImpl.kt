package dev.szymonchaber.checkstory.payments

import android.app.Activity
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import dev.szymonchaber.checkstory.domain.usecase.IsProUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class PurchaseSubscriptionUseCaseImpl @Inject constructor(private val billingManager: BillingManager) :
    PurchaseSubscriptionUseCase, IsProUserUseCase {

    private val _purchaseEvents = MutableSharedFlow<Either<PurchaseError, Purchase>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val purchaseEvents: Flow<Either<PurchaseError, Purchase>>
        get() = _purchaseEvents

    init {
        billingManager.purchasesUpdatedListener = { billingResult, purchases ->
            handlePurchaseResult(billingResult, purchases)
        }
    }

    override suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails> {
        return withContext(Dispatchers.Default) {
            billingManager.connectBillingClient().flatMap {
                fetchProductDetails(it, productId)
            }
        }
    }

    override fun startPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val firstOfferToken = productDetails.subscriptionOfferDetails!!.first().offerToken
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(firstOfferToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        val billingResult = billingManager.billingClient.launchBillingFlow(activity, flowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchaseEvents.tryEmit(mapPurchaseError(billingResult).left())
        }
    }

    private suspend fun fetchProductDetails(
        billingClient: BillingClient,
        productId: String
    ): Either<BillingError, ProductDetails> {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()

        val detailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(queryProductDetailsParams)
        }
        val billingResult = detailsResult.billingResult
        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            detailsResult.productDetailsList?.firstOrNull {
                it.productId == productId
            }?.right() ?: BillingError.NoProductsMatch(productId).left()
        } else {
            mapBillingError(billingResult).left()
        }
    }

    override suspend fun isProUser(): Boolean {
        return fetchCurrentSubscription().fold({
            false
        }
        ) {
            it != null
        }
    }

    suspend fun getPurchases() {
        billingManager.billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchaseList ->
            // Process the result
            val purchase = purchaseList.first()
            billingManager.billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) {
            }
        }
    }

    private suspend fun fetchCurrentSubscription(): Either<BillingError, Purchase?> {
        return billingManager.connectBillingClient().flatMap {
            suspendCoroutine { continuation ->
                billingManager.billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
                ) { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchases.firstOrNull().right()
                    } else {
                        mapBillingError(billingResult).left()
                    }.let(continuation::resume)
                }
            }
        }
    }

    private fun handlePurchaseResult(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.let {
                if (it.size == 1) {
                    handleSuccessfulPurchase(it.first())
                } else {
                    _purchaseEvents.tryEmit(PurchaseError.PurchaseListHasIncorrectSize(it.size).left())
                }
            } ?: _purchaseEvents.tryEmit(PurchaseError.PurchaseListIsNullDespiteStatusOK.left())
        } else {
            _purchaseEvents.tryEmit(mapPurchaseError(billingResult).left())
        }
    }

    private fun handleSuccessfulPurchase(purchase: Purchase) {
        billingManager.billingClient
            .acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            ) { billingResult ->
                val purchaseEvent = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchase.right()
                } else {
                    mapPurchaseError(billingResult).left()
                }
                _purchaseEvents.tryEmit(purchaseEvent)
            }
    }

    private fun mapBillingError(billingResult: BillingResult): BillingError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                throw IllegalArgumentException("OK billing result should be handled by the calling method")
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingError.BillingNotSupported
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingError.ConnectionError
            else -> BillingError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    private fun mapPurchaseError(billingResult: BillingResult): PurchaseError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                throw IllegalArgumentException("OK billing result should be handled by the calling method")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseError.UserCancelled
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> PurchaseError.ConnectionError
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseError.AlreadySubscribed
            else -> PurchaseError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }
}