package dev.szymonchaber.checkstory.payments

import android.app.Activity
import android.content.Context
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class PaymentInteractorImpl @Inject constructor(@ApplicationContext context: Context) : PaymentInteractor {

    var purchasesUpdatedListener: ((result: BillingResult, purchases: List<Purchase>?) -> Unit)? = null

    private val internalConnectionState = MutableStateFlow<InternalConnectionState>(InternalConnectionState.Idle)

    val billingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener { billingResult, purchases ->
            purchasesUpdatedListener?.invoke(billingResult, purchases)
        }
        .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun connectBillingClient(): Either<BillingError, BillingClient> {
        return internalConnectionState
            .onEach {
                when (it) {
                    InternalConnectionState.Idle -> {
                        internalConnectionState.emit(InternalConnectionState.Connecting)
                        if (billingClient.connectionState != ConnectionState.CONNECTING) {
                            startConnection()
                        }
                    }

                    else -> Unit
                }
            }
            .filterIsInstance<ConnectionResultState>()
            .flatMapLatest {
                when (it) {
                    InternalConnectionState.Connected -> {
                        flowOf(billingClient.right())
                    }

                    is InternalConnectionState.Error -> {
                        flowOf(it.error.left())
                    }
                }
            }
            .first()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing service connected")
                    internalConnectionState.tryEmit(InternalConnectionState.Connected)
                } else {
                    val mapBillingError = mapConnectionBillingError(billingResult)
                    Timber.e("Billing client setup failed: $mapBillingError")
                    internalConnectionState.tryEmit(InternalConnectionState.Error(mapBillingError))
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.d("Billing service disconnected")
                internalConnectionState.tryEmit(InternalConnectionState.Idle)
            }
        })
    }

    private fun mapConnectionBillingError(billingResult: BillingResult): BillingError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> throw IllegalArgumentException("OK billing result should be handled by the calling method")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingError.BillingNotSupported
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingError.ConnectionError(billingResult.debugMessage)
            else -> BillingError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    sealed interface ConnectionResultState

    sealed interface InternalConnectionState {

        object Idle : InternalConnectionState

        object Connecting : InternalConnectionState

        object Connected : InternalConnectionState, ConnectionResultState

        class Error(val error: BillingError) : InternalConnectionState, ConnectionResultState
    }

    // region interactor

    private val _purchaseEvents = MutableSharedFlow<Either<PurchaseError, Purchase>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val purchaseEvents: Flow<Either<PurchaseError, Purchase>>
        get() = _purchaseEvents

    init {
        purchasesUpdatedListener = { billingResult, purchases ->
            handlePurchaseResult(billingResult, purchases)
        }
    }

    // TODO rewire through the backend
    suspend fun isProUser(): Boolean {
        return fetchCurrentSubscription().fold({
            false
        }
        ) {
            it != null
        }
    }

    override suspend fun getPaymentPlans(): Either<BillingError, SubscriptionPlans> {
        return withContext(Dispatchers.Default) {
            connectBillingClient().flatMap {
                fetchAllProducts(it).map { productDetails ->
                    val monthlyProduct = productDetails
                        .first {
                            it.productId == PRODUCT_ID_PRO_MONTHLY
                        }
                        .toSubscriptionPlan(PlanDuration.MONTHLY)
                    val quarterlyProduct = productDetails
                        .first {
                            it.productId == PRODUCT_ID_PRO_QUARTERLY
                        }
                        .toSubscriptionPlan(PlanDuration.QUARTERLY)
                    val yearlyProduct = productDetails
                        .first {
                            it.productId == PRODUCT_ID_PRO_YEARLY
                        }
                        .toSubscriptionPlan(PlanDuration.YEARLY)
                    SubscriptionPlans(
                        monthlyProduct,
                        quarterlyProduct,
                        yearlyProduct
                    )
                }
            }
        }
    }

    private fun ProductDetails.toSubscriptionPlan(planDuration: PlanDuration): SubscriptionPlan {
        return subscriptionOfferDetails!!.first().let {
            val price = it.pricingPhases.pricingPhaseList.first().formattedPrice
            SubscriptionPlan(
                this,
                it.offerToken,
                planDuration,
                price
            )
        }
    }

    override suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails> {
        return withContext(Dispatchers.Default) {
            connectBillingClient().flatMap {
                fetchProductDetails(it, productId)
            }
        }
    }

    override fun startPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        val billingResult = billingClient.launchBillingFlow(activity, flowParams)
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

    private suspend fun fetchAllProducts(
        billingClient: BillingClient
    ): Either<BillingError, List<ProductDetails>> {
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder() // unused but returns empty list when not set
                            .setProductId("pro")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_ID_PRO_MONTHLY)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_ID_PRO_QUARTERLY)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_ID_PRO_YEARLY)
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
            detailsResult.productDetailsList?.right() ?: BillingError.NoProductsMatch("lol").left()
        } else {
            mapBillingError(billingResult).left()
        }
    }

    suspend fun getPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchaseList ->
            // Process the result
            val purchase = purchaseList.first()
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) {
            }
        }
    }

    private suspend fun fetchCurrentSubscription(): Either<BillingError, Purchase?> {
        return connectBillingClient().flatMap {
            suspendCoroutine { continuation ->
                billingClient.queryPurchasesAsync(
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
        billingClient
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
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingError.ConnectionError(billingResult.debugMessage)
            else -> BillingError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    private fun mapPurchaseError(billingResult: BillingResult): PurchaseError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                throw IllegalArgumentException("OK billing result should be handled by the calling method")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseError.UserCancelled
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> PurchaseError.ConnectionError(billingResult.debugMessage)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseError.AlreadySubscribed
            else -> PurchaseError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    companion object {

        private const val PRODUCT_ID_PRO_MONTHLY = "pro_monthly"
        private const val PRODUCT_ID_PRO_QUARTERLY = "pro_quarterly"
        private const val PRODUCT_ID_PRO_YEARLY = "pro_yearly"
    }
    // endregion
}
