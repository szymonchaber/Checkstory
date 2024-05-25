package dev.szymonchaber.checkstory.account

import com.firebase.ui.auth.IdpResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.interactor.AssignPaymentError
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.fold
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.domain.usecase.AssignPaymentToUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteAccountUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.LoginUseCase
import dev.szymonchaber.checkstory.domain.usecase.LogoutResult
import dev.szymonchaber.checkstory.domain.usecase.LogoutUseCase
import dev.szymonchaber.checkstory.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val tracker: Tracker,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val localPaymentRepository: PlayPaymentRepository,
    private val assignPaymentToUserUseCase: AssignPaymentToUserUseCase,
) : BaseViewModel<AccountEvent, AccountState, AccountEffect>(
    AccountState.initial
) {

    override fun buildMviFlow(eventFlow: Flow<AccountEvent>): Flow<Pair<AccountState?, AccountEffect?>> {
        return merge(
            eventFlow.handleLoadAccount(),
            eventFlow.handleTriggerRegistration(),
            eventFlow.handleFirebaseLoginClicked(),
            eventFlow.handleLogoutClicked(),
            eventFlow.handleLogoutDespiteUnsynchronizedDataClicked(),
            eventFlow.handleFirebaseResultReceived(),
            eventFlow.handleFirebaseAuthFlowCancelled(),
            eventFlow.handleRestorePaymentClicked(),
            eventFlow.handleManageSubscriptionsClicked(),
            eventFlow.handleSignUpClicked(),
            eventFlow.handleUpgradeClicked(),
            eventFlow.handleDeleteAccountClicked(),
            eventFlow.handleDeleteAccountConfirmed(),
        )
    }

    private fun Flow<AccountEvent>.handleLoadAccount(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoadAccount>() // TODO this could be combined with a map & a flatMap
            .map {
                AccountState(AccountLoadingState.Success(getCurrentUserUseCase.getCurrentUser()), false) to null
            }
    }

    private fun Flow<AccountEvent>.handleRestorePaymentClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.RestorePaymentClicked>()
            .map {
                val activeSubscription = localPaymentRepository.getActiveSubscription()
                when {
                    activeSubscription == null -> {
                        null to AccountEffect.ShowNoPurchasesFound
                    }

                    getCurrentUserUseCase.getCurrentUser().isLoggedIn -> {
                        assignPaymentToUserUseCase.assignPurchaseTokenToUser(activeSubscription.token).fold(
                            mapError = {
                                when (it) {
                                    AssignPaymentError.NetworkError -> {
                                        null to AccountEffect.ShowPurchaseRestorationFailed
                                    }

                                    AssignPaymentError.PurchaseTokenAssignedToAnotherUser -> {
                                        null to AccountEffect.ShowPurchaseAssignedToAnotherUser
                                    }
                                }
                            },
                            mapSuccess = {
                                state.value.copy(
                                    purchaseRestorationOngoing = false,
                                    accountLoadingState = AccountLoadingState.Success(getCurrentUserUseCase.getCurrentUser())
                                ) to AccountEffect.ShowPurchaseRestored
                            }
                        )
                    }

                    else -> {
                        state.value.copy(purchaseRestorationOngoing = true) to AccountEffect.StartAuthUi
                    }
                }
            }
    }

    // TODO extract classes dedicated to handling a given flow? (e.g. RestorePaymentHandler, ManageSubscriptionsHandler, etc.)

    private fun Flow<AccountEvent>.handleManageSubscriptionsClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.ManageSubscriptionsClicked>()
            .map {
                null to AccountEffect.NavigateToSubscriptionManagement
            }
    }

    private fun Flow<AccountEvent>.handleTriggerRegistration(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.TriggerPartialRegistration>()
            .map {
                AccountState(AccountLoadingState.Loading, true) to AccountEffect.StartAuthUi
            }
    }

    private fun Flow<AccountEvent>.handleFirebaseLoginClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoginClicked>()
            .mapWithState { state, _ ->
                state to AccountEffect.StartAuthUi
            }
    }

    private fun Flow<AccountEvent>.handleSignUpClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.SignUpClicked>()
            .mapWithState { state, _ ->
                state to AccountEffect.NavigateToPurchaseScreen
            }
    }

    private fun Flow<AccountEvent>.handleLogoutClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LogoutClicked>()
            .mapWithState { state, _ ->
                when (logoutUseCase.logoutSafely()) {
                    LogoutResult.Done -> {
                        state.copy(accountLoadingState = AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to null
                    }

                    LogoutResult.UnsynchronizedCommandsPresent -> {
                        _state.value to AccountEffect.ShowDataNotSynchronized
                    }
                }
            }
    }

    private fun Flow<AccountEvent>.handleLogoutDespiteUnsynchronizedDataClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LogoutDespiteUnsynchronizedDataClicked>()
            .mapWithState { state, _ ->
                logoutUseCase.logoutIgnoringUnsynchronizedData()
                state.copy(accountLoadingState = AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to null
            }
    }

    private fun Flow<AccountEvent>.handleUpgradeClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.UpgradeClicked>()
            .map {
                null to AccountEffect.NavigateToPurchaseScreen
            }
    }

    private fun Flow<AccountEvent>.handleDeleteAccountClicked(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.DeleteAccountClicked>()
            .map {
                null to AccountEffect.ShowConfirmDeleteAccountDialog
            }
    }

    private fun Flow<AccountEvent>.handleDeleteAccountConfirmed(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.DeleteAccountConfirmed>()
            .mapWithState { state, _ ->
                deleteAccountUseCase.deleteAccount()
                state.copy(accountLoadingState = AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to AccountEffect.ShowAccountDeleted
            }
    }

    private fun Flow<AccountEvent>.handleFirebaseResultReceived(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.FirebaseAuthResultReceived>()
            .withState()
            .flatMapLatest { (state, event) ->
                flow {
                    try {
                        val response = event.response
                        if (response.error != null) {
                            Timber.e(response.error)
                            emit(state to selectAuthErrorEvent(state))
                        } else {
                            emit(state.copy(accountLoadingState = AccountLoadingState.Loading) to null)
                            emit(handleFirebaseUiSuccess(state, response))
                        }
                    } catch (exception: Exception) {
                        Timber.e(exception)
                        state.copy(accountLoadingState = AccountLoadingState.Success(User.Guest())) to selectAuthErrorEvent(
                            state
                        )
                    }
                }
            }
    }

    private fun Flow<AccountEvent>.handleFirebaseAuthFlowCancelled(): Flow<Pair<AccountState?, AccountEffect?>> {
        return filterIsInstance<AccountEvent.FirebaseAuthFlowCancelled>()
            .withState()
            .mapLatest { (state, _) ->
                val effect = if (state.authForPaymentRequested) {
                    AccountEffect.ExitWithAuthResult(false, null)
                } else {
                    null
                }
                null to effect
            }
    }

    private suspend fun handleFirebaseUiSuccess(
        state: AccountState,
        response: IdpResponse
    ): Pair<AccountState, AccountEffect?> {
        return if (response.isNewUser) {
            register(state)
        } else {
            login(state)
        }
    }

    private fun selectAuthErrorEvent(state: AccountState): AccountEffect {
        return if (state.authForPaymentRequested) {
            AccountEffect.ExitWithAuthResult(false, null)
        } else {
            AccountEffect.ShowLoginNetworkError
        }
    }

    private suspend fun login(state: AccountState): Pair<AccountState, AccountEffect?> {
        return loginUseCase.login(state.purchaseRestorationOngoing)
            .fold(
                mapError = {
                    Timber.e(it.toString())
                    state.copy(
                        accountLoadingState = AccountLoadingState.Success(getCurrentUserUseCase.getCurrentUser()),
                        purchaseRestorationOngoing = false
                    ) to AccountEffect.ShowLoginNetworkError
                },
                mapSuccess = {
                    val effect = if (state.purchaseRestorationOngoing) {
                        AccountEffect.ShowPurchaseRestored
                    } else {
                        AccountEffect.ExitWithAuthResult(isSuccess = true, loggedInEmail = it.email)
                    }
                    state.copy(
                        accountLoadingState = AccountLoadingState.Success(it),
                        purchaseRestorationOngoing = false
                    ) to effect
                }
            )
    }

    private suspend fun register(state: AccountState): Pair<AccountState, AccountEffect?> {
        return registerUseCase.register()
            .fold(
                mapError = {
                    Timber.e(it.toString())
                    state.copy(accountLoadingState = AccountLoadingState.Success(getCurrentUserUseCase.getCurrentUser())) to AccountEffect.ShowLoginNetworkError
                },
                mapSuccess = {
                    val effect = if (state.purchaseRestorationOngoing) {
                        AccountEffect.ShowPurchaseRestored
                    } else {
                        AccountEffect.ExitWithAuthResult(true, it.email)
                    }
                    state.copy(accountLoadingState = AccountLoadingState.Success(it)) to effect
                }
            )
    }

    private fun <T> Flow<T>.mapWithState(
        block: suspend (AccountState, T) -> Pair<AccountState?, AccountEffect?>
    ): Flow<Pair<AccountState?, AccountEffect?>> {
        return withState().map { (state, event) ->
            block(state, event)
        }
    }

    private fun <T> Flow<T>.withState(): Flow<Pair<AccountState, T>> {
        return flatMapLatest { event ->
            state
                .map { it to event }
                .take(1)
        }
    }
}
