package dev.szymonchaber.checkstory.payments

import android.content.Context
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(@ApplicationContext context: Context) {

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
                    val mapBillingError = mapBillingError(billingResult)
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

    private fun mapBillingError(billingResult: BillingResult): BillingError {
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
}
