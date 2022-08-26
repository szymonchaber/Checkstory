package dev.szymonchaber.checkstory.payments

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.payments.model.PaymentEffect
import dev.szymonchaber.checkstory.payments.model.PaymentEvent
import dev.szymonchaber.checkstory.payments.model.PaymentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val tracker: Tracker,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase
) : BaseViewModel<
        PaymentEvent,
        PaymentState,
        PaymentEffect
        >(
    PaymentState.initial
) {

    init {
        purchaseSubscriptionUseCase.purchaseEvents
            .onEach {
                onEvent(PaymentEvent.NewPurchaseResult(it))
            }
            .launchIn(viewModelScope)
        onEvent(PaymentEvent.LoadSubscriptionPlans)
    }

    override fun buildMviFlow(eventFlow: Flow<PaymentEvent>): Flow<Pair<PaymentState, PaymentEffect?>> {
        return merge(
            eventFlow.handleLoadSubscriptionPlans(),
            eventFlow.handleBuyClicked(),
            eventFlow.handlePurchaseEvents(),
        )
    }

    private fun Flow<PaymentEvent>.handleLoadSubscriptionPlans(): Flow<Pair<PaymentState, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.LoadSubscriptionPlans>()
            .map {
                val plans = listOf(
                    SubscriptionPlan(PlanDuration(1, PlanDurationUnit.MONTH), "$8.99", "$8.99/mo"),
                    SubscriptionPlan(PlanDuration(12, PlanDurationUnit.MONTH), "$85,99", "$6.99/mo"),
                    SubscriptionPlan(PlanDuration(3, PlanDurationUnit.MONTH), "$25,99", "$8.69/mo"),
                )
                PaymentState(
                    paymentLoadingState = PaymentState.PaymentLoadingState.Success(
                        "idle",
                        plans,
                        null
                    )
                ) to null
            }
    }

    private fun Flow<PaymentEvent>.handlePurchaseEvents(): Flow<Pair<PaymentState, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.NewPurchaseResult>()
            .map { result ->
                Timber.d("Got purchase details or error: $result")
                PaymentState(
                    result = result.toString(),
                    paymentLoadingState = PaymentState.PaymentLoadingState.Loading
                ) to null
            }
    }

    private fun Flow<PaymentEvent>.handleBuyClicked(): Flow<Pair<PaymentState, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.BuyClicked>()
            .onEach {
//                tracker.logEvent("buy_clicked")
            }
            .map { event ->
                val textValue = purchaseSubscriptionUseCase.getProductDetails("pro")
                    .map {
                        purchaseSubscriptionUseCase.startPurchaseFlow(event.activity, it)
                    }
                    .fold({
                        it.toString()
                    }, { it.toString() })
                Timber.d("Got details or error: $textValue")
                PaymentState(result = textValue, paymentLoadingState = PaymentState.PaymentLoadingState.Loading) to null
            }
    }
}
