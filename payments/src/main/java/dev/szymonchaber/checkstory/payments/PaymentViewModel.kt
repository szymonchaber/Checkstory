package dev.szymonchaber.checkstory.payments

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.IsProUserUseCase
import dev.szymonchaber.checkstory.payments.model.PaymentEffect
import dev.szymonchaber.checkstory.payments.model.PaymentEvent
import dev.szymonchaber.checkstory.payments.model.PaymentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
    private val isProUserUseCase: IsProUserUseCase,
    private val getPaymentPlansUseCase: GetPaymentPlansUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val refreshPaymentInformationUseCase: RefreshPaymentInformationUseCase
) : BaseViewModel<
        PaymentEvent,
        PaymentState<out PaymentState.PaymentLoadingState>,
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

    override fun buildMviFlow(eventFlow: Flow<PaymentEvent>): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return merge(
            eventFlow.handleLoadSubscriptionPlans(),
            eventFlow.handlePlanSelected(),
            eventFlow.handleBuyClicked(),
            eventFlow.handlePurchaseEvents(),
            eventFlow.handleContinueClicked()
        )
    }

    private fun Flow<PaymentEvent>.handleLoadSubscriptionPlans(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.LoadSubscriptionPlans>()
            .flatMapLatest {
                flow {
                    emit(
                        PaymentState(
                            paymentLoadingState = PaymentState.PaymentLoadingState.Loading
                        ) to null
                    )
                    if (isProUserUseCase.isProUser()) {
                        emit(PaymentState(paymentLoadingState = PaymentState.PaymentLoadingState.Paid) to null)
                    } else {
                        val paymentLoadingState = getPaymentPlansUseCase.getPaymentPlans()
                            .fold({
                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("Fetching payment plans failed!\n$it"));
                                Timber.e(it.toString())
                                PaymentState.PaymentLoadingState.LoadingError
                            }
                            ) {
                                PaymentState.PaymentLoadingState.Success(
                                    plans = it,
                                    selectedPlan = it.yearly,
                                    paymentInProgress = false
                                )
                            }
                        emit(PaymentState(paymentLoadingState = paymentLoadingState) to null)
                    }
                }
            }
    }

    private fun Flow<PaymentEvent>.handlePlanSelected(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.PlanSelected>()
            .withSuccessState()
            .map { (state, event) ->
                tracker.logEvent(
                    "payment_plan_selected", getSelectedPlanMetadata(event.subscriptionPlan.planDuration)
                )
                val paymentLoadingState = state.paymentLoadingState.copy(selectedPlan = event.subscriptionPlan)
                state.copy(paymentLoadingState = paymentLoadingState) to null
            }
    }

    private fun getSelectedPlanMetadata(planDuration: PlanDuration): Bundle {
        val selectedPlanTrackingName = when (planDuration) {
            PlanDuration.MONTHLY -> "monthly"
            PlanDuration.QUARTERLY -> "quarterly"
            PlanDuration.YEARLY -> "yearly"
        }
        return bundleOf(
            "plan_duration" to selectedPlanTrackingName
        )
    }

    private fun Flow<PaymentEvent>.handleBuyClicked(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.BuyClicked>()
            .onEach {
            }
            .withSuccessState()
            .map { (state, event) ->
                val loadingState = state.paymentLoadingState
                tracker.logEvent(
                    "buy_button_clicked",
                    getSelectedPlanMetadata(state.paymentLoadingState.selectedPlan.planDuration)
                )
                purchaseSubscriptionUseCase.startPurchaseFlow(
                    event.activity,
                    loadingState.selectedPlan.productDetails,
                    loadingState.selectedPlan.offerToken
                )
                state.copy(paymentLoadingState = loadingState.copy(paymentInProgress = true)) to null
            }
    }

    private fun Flow<PaymentEvent>.handlePurchaseEvents(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.NewPurchaseResult>()
            .withSuccessState()
            .map { (state, event) ->
                Timber.d("Got purchase details or error: ${event.paymentResult}")
                event.paymentResult
                    .tap {
                        refreshPaymentInformationUseCase.refreshPaymentInformation()
                    }
                    .fold({
                        Timber.e(it.toString())
                        FirebaseCrashlytics.getInstance().recordException(Exception("Purchase attempt failed!\n$it"))
                        val effect = if (it == PurchaseError.UserCancelled) {
                            null
                        } else {
                            PaymentEffect.PaymentError()
                        }
                        state.copy(state.paymentLoadingState.copy(paymentInProgress = false)) to effect
                    }, {
                        tracker.logEvent(
                            "subscription_success",
                            getSelectedPlanMetadata(state.paymentLoadingState.selectedPlan.planDuration)
                        )
                        PaymentState(PaymentState.PaymentLoadingState.Paid) to null
                    })
            }
    }

    private fun Flow<PaymentEvent>.handleContinueClicked(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.ContinueClicked>()
            .map {
                _state.value to PaymentEffect.ExitPaymentScreen()
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<PaymentState<PaymentState.PaymentLoadingState.Success>, T>> {
        return flatMapLatest { event ->
            state
                .filterIsInstance<PaymentState<PaymentState.PaymentLoadingState.Success>>()
                .map { it to event }
                .take(1)
        }
    }
}
