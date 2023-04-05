package dev.szymonchaber.checkstory.payments

import android.content.Context
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingManager @Inject constructor(@ApplicationContext context: Context) {

    var purchasesUpdatedListener: ((result: BillingResult, purchases: List<Purchase>?) -> Unit)? = null

    val billingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener { billingResult, purchases ->
            purchasesUpdatedListener?.invoke(billingResult, purchases)
        }
        .build()

    suspend fun connectBillingClient(): Either<BillingError, BillingClient> {
        return suspendCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.d("Billing service connected")
                        continuation.resume(billingClient.right())
                    } else {
                        val mapBillingError = mapBillingError(billingResult)
                        Timber.e("Billing client setup failed: $mapBillingError")
                        continuation.resume(mapBillingError.left())
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Timber.d("Billing service disconnected")
//                    continuation.resumeWithException(IllegalStateException("Billing service disconnected")) TODO see if lack of this breaks anything
                }
            })
        }
    }

    private fun mapBillingError(billingResult: BillingResult): BillingError {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> throw IllegalArgumentException("OK billing result should be handled by the calling method")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingError.BillingNotSupported
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingError.ConnectionError(billingResult.debugMessage)
            else -> BillingError.Unhandled(billingResult.responseCode, billingResult.debugMessage)
        }
    }
}
