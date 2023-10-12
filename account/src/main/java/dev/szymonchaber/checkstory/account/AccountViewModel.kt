package dev.szymonchaber.checkstory.account

import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.fold
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
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val tracker: Tracker,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<AccountEvent, AccountState, AccountEffect>(
    AccountState.initial
) {

    private val firebaseAuth by lazy { Firebase.auth }

    override fun buildMviFlow(eventFlow: Flow<AccountEvent>): Flow<Pair<AccountState?, AccountEffect?>> {
        return merge( // TODO this could be made into a dsl like on<AccountEvent.LoadAccount>.flatMap... etc. maybe?
            eventFlow.handleLoadAccount(),
            eventFlow.handleTriggerPartialRegistration(),
            eventFlow.handleFirebaseLoginClicked(),
            eventFlow.handleLoginClicked(),
            eventFlow.handleRegisterClicked(),
            eventFlow.handleLogoutClicked(),
            eventFlow.handleLogoutDespiteUnsynchronizedDataClicked(),
            eventFlow.handleFirebaseResultReceived()
        )
    }

    private fun Flow<AccountEvent>.handleLoadAccount(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoadAccount>() // TODO this could be combined with a map & a flatMap
            .map {
                AccountState(AccountLoadingState.Success(getCurrentUserUseCase.getCurrentUser()), false) to null
            }
    }

    private fun Flow<AccountEvent>.handleTriggerPartialRegistration(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.TriggerPartialRegistration>()
            .map {
                AccountState(AccountLoadingState.Loading, true) to AccountEffect.StartAuthUi()
            }
    }

    private fun Flow<AccountEvent>.handleFirebaseLoginClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.FirebaseLoginClicked>()
            .mapWithState { state, _ ->
                state to AccountEffect.StartAuthUi()
            }
    }

    private fun Flow<AccountEvent>.handleLogoutClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LogoutClicked>()
            .mapWithState { state, _ ->
                when (logoutUseCase.logoutSafely()) {
                    LogoutResult.Done -> {
                        firebaseAuth.signOut()
                        state.copy(accountLoadingState = AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to null
                    }

                    LogoutResult.UnsynchronizedCommandsPresent -> {
                        _state.value to AccountEffect.ShowDataNotSynchronized()
                    }
                }
            }
    }

    private fun Flow<AccountEvent>.handleLogoutDespiteUnsynchronizedDataClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LogoutDespiteUnsynchronizedDataClicked>()
            .mapWithState { state, _ ->
                logoutUseCase.logoutIgnoringUnsynchronizedData()
                state.copy(accountLoadingState = AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to null
            }
    }

    private fun Flow<AccountEvent>.handleLoginClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoginClicked>()
            .withState()
            .flatMapLatest { (state, it) ->
                flow {
                    emit(state.copy(accountLoadingState = AccountLoadingState.Loading) to null)
                    emit(
                        try {
                            firebaseAuth.signInWithEmailAndPassword(it.email, it.password).await()
                            if (state.partialAuthRequested) {
                                state to AccountEffect.ExitWithAuthResult(true)
                            } else {
                                loginUseCase.login()
                                    .fold(
                                        mapError = {
                                            Timber.e(it.toString())
                                            state.copy(accountLoadingState = AccountLoadingState.Loading) to AccountEffect.ShowLoginNetworkError()
                                        },
                                        mapSuccess = {
                                            state.copy(accountLoadingState = AccountLoadingState.Success(it)) to null
                                        }
                                    )
                            }
                        } catch (exception: Exception) {
                            val event = selectAuthErrorEvent(state)
                            state.copy(accountLoadingState = AccountLoadingState.Success(User.Guest())) to event
                        }
                    )
                }
            }
    }

    private fun Flow<AccountEvent>.handleRegisterClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.RegisterClicked>()
            .withState()
            .flatMapLatest { (state, event) ->
                flow {
                    emit(state.copy(accountLoadingState = AccountLoadingState.Loading) to null)
                    emit(
                        try {
                            firebaseAuth.signOut()
                            firebaseAuth.createUserWithEmailAndPassword(event.email, event.password).await()
                            if (state.partialAuthRequested) {
                                state to AccountEffect.ExitWithAuthResult(true)
                            } else {
                                register(state)
                            }
                        } catch (exception: Exception) {
                            Timber.e(exception)
                            state.copy(accountLoadingState = AccountLoadingState.Success(User.Guest())) to selectAuthErrorEvent(
                                state
                            )
                        }
                    )
                }
            }
    }

    private fun Flow<AccountEvent>.handleFirebaseResultReceived(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.FirebaseResultReceived>()
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

    private suspend fun handleFirebaseUiSuccess(
        state: AccountState,
        response: IdpResponse
    ): Pair<AccountState, AccountEffect?> {
        return if (state.partialAuthRequested) {
            state to AccountEffect.ExitWithAuthResult(true)
        } else {
            if (response.isNewUser) {
                register(state)
            } else {
                login(state)
            }
        }
    }

    private fun selectAuthErrorEvent(state: AccountState): AccountEffect {
        return if (state.partialAuthRequested) {
            AccountEffect.ExitWithAuthResult(false)
        } else {
            AccountEffect.ShowLoginNetworkError()
        }
    }

    private suspend fun login(state: AccountState): Pair<AccountState, AccountEffect?> {
        return loginUseCase.login()
            .fold(
                mapError = {
                    Timber.e(it.toString())
                    state.copy(accountLoadingState = AccountLoadingState.Loading) to AccountEffect.ShowLoginNetworkError()
                },
                mapSuccess = {
                    state.copy(accountLoadingState = AccountLoadingState.Success(it)) to null
                }
            )
    }

    private suspend fun register(state: AccountState): Pair<AccountState, AccountEffect?> {
        return registerUseCase.register()
            .fold(
                mapError = {
                    Timber.e(it.toString())
                    state.copy(accountLoadingState = AccountLoadingState.Loading) to AccountEffect.ShowLoginNetworkError()
                },
                mapSuccess = {
                    state.copy(accountLoadingState = AccountLoadingState.Success(it)) to null
                }
            )
    }

    private fun <T> Flow<T>.mapWithState(
        block: suspend (AccountState, T) -> Pair<AccountState, AccountEffect?>
    ): Flow<Pair<AccountState, AccountEffect?>> {
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
