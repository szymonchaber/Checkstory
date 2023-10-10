package dev.szymonchaber.checkstory.payments.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleError
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.szymonchaber.checkstory.domain.model.payment.ActiveSubscription
import dev.szymonchaber.checkstory.domain.model.payment.PurchaseToken
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.domain.repository.SubscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class BillingInteractorImpl @Inject constructor(@ApplicationContext private val context: Context) :
    PlayPaymentRepository, DefaultLifecycleObserver {

    private lateinit var billingClient: BillingClient

    private val _subscriptionPlans = MutableStateFlow<Either<BillingError, SubscriptionPlans>?>(null)
    internal val subscriptionPlans: Flow<Either<BillingError, SubscriptionPlans>?>
        get() = _subscriptionPlans

    private val _subscriptionStatusFlow = MutableStateFlow<SubscriptionStatus?>(null)

    override val subscriptionStatusFlow: Flow<SubscriptionStatus?>
        get() = _subscriptionStatusFlow

    private val _purchaseEvents = MutableSharedFlow<Either<PurchaseError, Purchase>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun onCreate(owner: LifecycleOwner) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(::handlePurchaseResult)
            .enablePendingPurchases()
            .build()
        if (!billingClient.isReady) {
            billingClient.startConnection(object : BillingClientStateListener {

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.d("Billing service connected")
                        prefetchData()
                    } else {
                        val mapBillingError = mapConnectionBillingError(billingResult)
                        Timber.e("Billing client setup failed: $mapBillingError")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Timber.d("Billing service disconnected")
                }
            })
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (billingClient.isReady) {
            Timber.d("BillingClient can only be used once -- closing connection")
            billingClient.endConnection()
        }
    }

    private fun prefetchData() {
        GlobalScope.launch {
            try {
                _subscriptionPlans.emit(getPaymentPlans().tapLeft { Timber.e("Prefetching plans failed: $it") })
                _subscriptionStatusFlow.emit(fetchCurrentSubscription()
                    .map {
                        it?.purchaseToken?.let { purchaseToken ->
                            SubscriptionStatus.Active(ActiveSubscription(PurchaseToken(purchaseToken)))
                        } ?: SubscriptionStatus.Inactive
                    }
                    .tapLeft {
                        Timber.e("Prefetching active subscription failed: $it")
                    }
                    .orNull()
                )
            } catch (exception: Exception) {
                Timber.e(exception)
            }
        }
    }

    private fun connectBillingClient(): Either<BillingError, BillingClient> {
        return if (billingClient.isReady) {
            billingClient.right()
        } else {
            BillingError.BillingClientConnectionError.left()
        }
    }

    private fun mapConnectionBillingError(billingResult: BillingResult): BillingError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> throw IllegalArgumentException("OK billing result should be handled by the calling method")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingError.BillingNotSupported
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingError.ConnectionError(billingResult.debugMessage)
            else -> BillingError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    // region interactor

    override suspend fun getActiveSubscription(): ActiveSubscription? {
        return fetchCurrentSubscription()
            .map { purchase ->
                purchase?.purchaseToken?.let { ActiveSubscription(PurchaseToken(it)) }
            }
            .tap {
                val status = it?.let(SubscriptionStatus::Active) ?: SubscriptionStatus.Inactive
                _subscriptionStatusFlow.tryEmit(status)
            }
            .handleError {
                when (val status = _subscriptionStatusFlow.value) {
                    is SubscriptionStatus.Active -> status.activeSubscription
                    SubscriptionStatus.Inactive -> null
                    null -> null
                }
            }
            .orNull()
    }

    private suspend fun getPaymentPlans(): Either<BillingError, SubscriptionPlans> {
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

    internal suspend fun getProductDetails(productId: String): Either<BillingError, ProductDetails> {
        return withContext(Dispatchers.Default) {
            connectBillingClient().flatMap {
                fetchProductDetails(it, productId)
            }
        }
    }

    internal fun startPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String
    ): Flow<Either<PurchaseError, Purchase>> {
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
        return _purchaseEvents
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
                }.tap {
                    _subscriptionStatusFlow.tryEmit(SubscriptionStatus.Active(ActiveSubscription(PurchaseToken(it.purchaseToken))))
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
