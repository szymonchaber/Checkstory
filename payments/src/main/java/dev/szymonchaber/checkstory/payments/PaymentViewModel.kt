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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val tracker: Tracker,
    private val getPaymentPlansUseCase: GetPaymentPlansUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
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
            eventFlow.handlePlanSelected(),
            eventFlow.handleBuyClicked(),
            eventFlow.handlePurchaseEvents(),
        )
    }

    private fun Flow<PaymentEvent>.handleLoadSubscriptionPlans(): Flow<Pair<PaymentState, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.LoadSubscriptionPlans>()
            .map {
                getPaymentPlansUseCase.getPaymentPlans().fold({
                    PaymentState(
                        paymentLoadingState = PaymentState.PaymentLoadingState.Error
                    )
                }, {
                    PaymentState(
                        paymentLoadingState = PaymentState.PaymentLoadingState.Success("idle", it, null)
                    )
                }) to null
            }
    }

    private fun Flow<PaymentEvent>.handlePlanSelected(): Flow<Pair<PaymentState, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.PlanSelected>()
            .withSuccessState()
            .map { (state, event) ->
                val paymentLoadingState = state.copy(selectedPlan = event.subscriptionPlan)
                PaymentState(paymentLoadingState = paymentLoadingState) to null
            }
    }

    private fun Flow<PaymentEvent>.handleBuyClicked(): Flow<Pair<PaymentState, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.BuyClicked>()
            .onEach {
//                tracker.logEvent("buy_clicked")
            }
            .withSuccessState()
            .map { (state, event) ->
                state.selectedPlan?.let {
                    purchaseSubscriptionUseCase.startPurchaseFlow(
                        event.activity,
                        it.productDetails,
                        it.offerToken
                    )
                    PaymentState(paymentLoadingState = state.copy(paymentInProgress = true)) to null
                } ?: (PaymentState(state) to null)
            }
    }

    private fun Flow<PaymentEvent>.handlePurchaseEvents(): Flow<Pair<PaymentState, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.NewPurchaseResult>()
            .withSuccessState()
            .map { (state, event) ->
                Timber.d("Got purchase details or error: ${event.paymentResult}")
                PaymentState(
                    paymentLoadingState = state.copy(
                        result = event.paymentResult.toString(),
                        paymentInProgress = false
                    )
                ) to null
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<PaymentState.PaymentLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.paymentLoadingState }
                .filterIsInstance<PaymentState.PaymentLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }
}
