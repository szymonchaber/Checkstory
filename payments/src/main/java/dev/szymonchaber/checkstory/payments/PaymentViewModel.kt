package dev.szymonchaber.checkstory.payments

import android.os.Bundle
import androidx.core.os.bundleOf
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.android.billingclient.api.Purchase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.fold
import dev.szymonchaber.checkstory.domain.model.payment.PurchaseToken
import dev.szymonchaber.checkstory.domain.usecase.AssignPaymentToUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.payments.billing.PlanDuration
import dev.szymonchaber.checkstory.payments.billing.PurchaseError
import dev.szymonchaber.checkstory.payments.model.PaymentEffect
import dev.szymonchaber.checkstory.payments.model.PaymentEvent
import dev.szymonchaber.checkstory.payments.model.PaymentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class PaymentViewModel @Inject constructor(
    private val tracker: Tracker,
    private val getUserUseCase: GetCurrentUserUseCase,
    private val getPaymentPlansUseCase: GetPaymentPlansUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val assignPaymentToUserUseCase: AssignPaymentToUserUseCase
) : BaseViewModel<
        PaymentEvent,
        PaymentState<out PaymentState.PaymentLoadingState>,
        PaymentEffect
        >(
    PaymentState.initial
) {

    init {
        onEvent(PaymentEvent.LoadSubscriptionPlans)
    }

    override fun buildMviFlow(eventFlow: Flow<PaymentEvent>): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return merge(
            eventFlow.handleLoadSubscriptionPlans(),
            eventFlow.handlePlanSelected(),
            eventFlow.handleBuyClicked(),
            eventFlow.handleRegistrationSuccess(),
            eventFlow.handleRegistrationCancelled()
        )
    }

    private fun Flow<PaymentEvent>.handleLoadSubscriptionPlans(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.LoadSubscriptionPlans>()
            .flatMapLatest {
                flow {
                    emit(PaymentState(PaymentState.PaymentLoadingState.Loading) to null)
                    if (getUserUseCase.getCurrentUser().isPaidUser) {
                        emit(PaymentState(paymentLoadingState = PaymentState.PaymentLoadingState.Paid) to null)
                    } else {
                        emitAll(getPaymentPlansUseCase.getPaymentPlans()
                            .filterNotNull()
                            .map { result ->
                                result.fold(
                                    ifLeft = {
                                        FirebaseCrashlytics.getInstance()
                                            .recordException(Exception("Fetching payment plans failed!\n$it"));
                                        Timber.e(it.toString())
                                        PaymentState.PaymentLoadingState.LoadingError
                                    },
                                    ifRight = PaymentState.PaymentLoadingState::success
                                )
                            }.map {
                                PaymentState(paymentLoadingState = it) to null
                            })
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
            .mapLatest { (state, _) ->
                val loadingState = state.paymentLoadingState
                tracker.logEvent(
                    "buy_button_clicked",
                    getSelectedPlanMetadata(state.paymentLoadingState.selectedPlan.planDuration)
                )
                state.copy(paymentLoadingState = loadingState.copy(paymentInProgress = true)) to PaymentEffect.NavigateToRegistration()
            }
    }

    private fun Flow<PaymentEvent>.handleRegistrationSuccess(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.RegistrationSuccess>()
            .withSuccessState()
            .flatMapLatest { (state, event) ->
                flow {
                    val loadingState = state.paymentLoadingState
                    tracker.logEvent("payment_flow_registration_success")
                    emitAll(
                        purchaseSubscriptionUseCase.startPurchaseFlow(
                            event.activity,
                            loadingState.selectedPlan.productDetails,
                            loadingState.selectedPlan.offerToken
                        ).map {
                            it.flatMap { purchase ->
                                assignPaymentToUserUseCase.assignPurchaseTokenToUser(PurchaseToken(purchase.purchaseToken))
                                    .fold(
                                        mapError = {
                                            PurchaseError.CheckstoryBackendConnectionError(it).left()
                                        },
                                        mapSuccess = {
                                            purchase.right()
                                        }
                                    )
                            }
                        }
                            .handlePurchaseEvents()
                    )
                }
            }
    }

    private fun Flow<PaymentEvent>.handleRegistrationCancelled(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return filterIsInstance<PaymentEvent.RegistrationCancelled>()
            .withSuccessState()
            .mapLatest { (state, _) ->
                tracker.logEvent("payment_flow_registration_cancelled")
                state.copy(paymentLoadingState = state.paymentLoadingState.copy(paymentInProgress = false)) to null
            }
    }

    private fun Flow<Either<PurchaseError, Purchase>>.handlePurchaseEvents(): Flow<Pair<PaymentState<*>, PaymentEffect?>> {
        return withSuccessState()
            .map { (state, event) ->
                event
                    .fold(
                        ifLeft = {
                            Timber.e(it.toString())
                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("Purchase attempt failed!\n$it"))
                            val effect = if (it == PurchaseError.UserCancelled) {
                                null
                            } else {
                                PaymentEffect.PaymentError()
                            }
                            state.copy(state.paymentLoadingState.copy(paymentInProgress = false)) to effect
                        },
                        ifRight = {
                            tracker.logEvent(
                                "subscription_success",
                                getSelectedPlanMetadata(state.paymentLoadingState.selectedPlan.planDuration)
                            )
                            PaymentState(PaymentState.PaymentLoadingState.Paid) to PaymentEffect.NavigateToPaymentSuccess()
                        }
                    )
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
