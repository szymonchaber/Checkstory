package dev.szymonchaber.checkstory.payments

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.querySkuDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class PurchaseSubscriptionUseCase @Inject constructor(private val billingManager: BillingManager) {

    val purchaseEvents = MutableSharedFlow<Either<PurchaseError, Purchase>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails> {
        return withContext(Dispatchers.Default) {
            Timber.d("Fetching productId: $productId")
            billingManager.connectBillingClient().flatMap {
                fetchProductDetails(it, productId)
            }
        }
    }

//    fun getSkuDetailsNew(sku: String): Flow<Either<BillingError, SkuDetails>> {
//        val productList =
//            listOf(
//                QueryProductDetailsParams.Product.newBuilder()
//                    .setProductId("up_basic_sub")
//                    .setProductType(BillingClient.ProductType.SUBS)
//                    .build()
//            )
//
//        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
//
//        billingClient.queryProductDetailsAsync(params.build()) { billingResult,
//                                                                 productDetailsList ->
//            // Process the result
//        }
//
//        return flow {
//            connectBillingClient()
//                .flatMap {
//                    val skuList = ArrayList<String>()
//                    skuList.add(sku)
//                    val params1 = SkuDetailsParams.newBuilder()
//                    params1.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
//                    val skuDetailsResult = withContext(Dispatchers.IO) {
//                        billingClient.querySkuDetails(params1.build())
//                    }
//                    val billingResult = skuDetailsResult.billingResult
//                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                        skuDetailsResult.skuDetailsList?.firstOrNull {
//                            it.sku == sku
//                        }?.right() ?: BillingError.IncorrectSkuProvided(sku).left()
//                    } else {
//                        mapBillingError(billingResult).left()
//                    }
//                }.emitIn(this)
//        }.flowOn(Dispatchers.Default)
//    }

//    fun startPurchaseFlow(activity: Activity, skuDetails: SkuDetails) {
//        val flowParams = BillingFlowParams.newBuilder()
//            .setSkuDetails(skuDetails)
//            .build()
//        launchBillingFlow(activity, flowParams)
//    }

//    fun startNewPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
//// Retrieve a value for "productDetails" by calling queryProductDetailsAsync()
//// Get the offerToken of the selected offer
//        val selectedOfferIndex = 0;
//        val offerToken = productDetails.subscriptionOfferDetails?.get(selectedOfferIndex)?.offerToken
//        offerToken?.let {
//
//
//            val productDetailsParamsList =
//                listOf(
//                    BillingFlowParams.ProductDetailsParams.newBuilder()
//                        .setProductDetails(productDetails)
//                        .setOfferToken(it)
//                        .build()
//                )
//            val billingFlowParams =
//                BillingFlowParams.newBuilder()
//                    .setProductDetailsParamsList(productDetailsParamsList)
//                    .build()
//
//// Launch the billing flow
//            val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
//            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//                purchaseEvents.tryEmit(mapPurchaseError(billingResult).left())
//            }
//        }
//    }

//    fun startUpgradeFlow(): Flow<Either<BillingError, Unit>> {
//        return flow {
//            fetchCurrentSubscription()
//                .flatMap {
//                    populateWithSkuDetails(it)
//                }
//                .map {
//                }
//                .emitIn(this)
//        }.flowOn(Dispatchers.IO)
//    }

//    suspend fun getPurchases() {
//        billingClient.queryPurchasesAsync(
//            QueryPurchasesParams.newBuilder()
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
//        ) { billingResult, purchaseList ->
//            // Process the result
//            val purchase = purchaseList.first()
//            billingClient.acknowledgePurchase(
//                AcknowledgePurchaseParams.newBuilder()
//                    .setPurchaseToken(purchase.purchaseToken)
//                    .build()
//            ) {
//            }
//        }
//    }

//    private suspend fun populateWithSkuDetails(purchase: Purchase): Either<BillingError, Pair<Purchase, SkuDetails>> {
//        return connectBillingClient()
//            .flatMap {
//                fetchSkuDetails(it, purchase.products.first())
//            }.map {
//                purchase to it
//            }
//    }

//    private suspend fun fetchCurrentSubscription(): Either<BillingError, Purchase> {
//        return suspendCoroutine { continuation ->
//            billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, purchases ->
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    purchases.firstOrNull()?.right() ?: BillingError.Unhandled(-1, "no purchases to upgrade").left()
//                } else {
//                    mapBillingError(billingResult).left()
//                }.let(continuation::resume)
//            }
//        }
//    }

//    private fun launchBillingFlow(activity: Activity, flowParams: BillingFlowParams) {
//        val billingResult = billingClient.launchBillingFlow(activity, flowParams)
//        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//            purchaseEvents.tryEmit(mapPurchaseError(billingResult).left())
//        }
//    }

//    private fun handlePurchaseResult(billingResult: BillingResult, purchases: List<Purchase>?) {
//        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//            purchases?.let {
//                if (it.size == 1) {
//                    handleSuccessfulPurchase(it.first())
//                } else {
//                    purchaseEvents.tryEmit(PurchaseError.PurchaseListHasIncorrectSize(it.size).left())
//                }
//            } ?: purchaseEvents.tryEmit(PurchaseError.PurchaseListIsNullDespiteStatusOK.left())
//        } else {
//            purchaseEvents.tryEmit(mapPurchaseError(billingResult).left())
//        }
//    }

    private fun handleSuccessfulPurchase(purchase: Purchase) {
        // TODO call the API to confirm a purchase
        purchaseEvents.tryEmit(purchase.right())
    }


    private suspend fun fetchSkuDetails(billingClient: BillingClient, sku: String): Either<BillingError, SkuDetails> {
        val skuList = ArrayList<String>()
        skuList.add(sku)

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }
        val billingResult = skuDetailsResult.billingResult
        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            skuDetailsResult.skuDetailsList?.firstOrNull {
                it.sku == sku
            }?.right() ?: BillingError.NoProductsMatch(sku).left()
        } else {
            mapBillingError(billingResult).left()
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
            }?.right() ?: BillingError.IncorrectSkuProvided(productId).left()
        } else {
            mapBillingError(billingResult).left()
        }
    }

    private fun mapBillingError(billingResult: BillingResult): BillingError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> throw IllegalArgumentException("OK billing result should be handled by the calling method")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingError.BillingNotSupported
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingError.ConnectionError
            else -> BillingError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    private fun mapPurchaseError(billingResult: BillingResult): PurchaseError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> throw IllegalArgumentException("OK billing result should be handled by the calling method")
            BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseError.UserCancelled
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> PurchaseError.ConnectionError
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseError.AlreadySubscribed
            else -> PurchaseError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }
}

suspend fun <T> T.emitIn(flowCollector: FlowCollector<T>) {
    flowCollector.emit(this)
}
