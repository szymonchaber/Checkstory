package dev.szymonchaber.checkstory.account

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

    private val auth by lazy { Firebase.auth }

    override fun buildMviFlow(eventFlow: Flow<AccountEvent>): Flow<Pair<AccountState?, AccountEffect?>> {
        return merge(
            eventFlow.handleLoadAccount(),
            eventFlow.handleLoginClicked(),
            eventFlow.handleRegisterClicked(),
            eventFlow.handleLogoutClicked(),
            eventFlow.handleLogoutDespiteUnsynchronizedDataClicked()
        )
    }

    private fun Flow<AccountEvent>.handleLoadAccount(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoadAccount>()
            .map {
                AccountState(AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to null
            }
    }

    private fun Flow<AccountEvent>.handleLogoutClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LogoutClicked>()
            .map {
                when (logoutUseCase.logoutSafely()) {
                    LogoutResult.Done -> {
                        auth.signOut()
                        AccountState(AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to null
                    }

                    LogoutResult.UnsynchronizedCommandsPresent -> {
                        _state.value to AccountEffect.ShowDataNotSynchronized()
                    }
                }
            }
    }

    private fun Flow<AccountEvent>.handleLogoutDespiteUnsynchronizedDataClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LogoutDespiteUnsynchronizedDataClicked>()
            .map {
                logoutUseCase.logoutIgnoringUnsynchronizedData()
                AccountState(AccountLoadingState.Success(user = getCurrentUserUseCase.getCurrentUser())) to null
            }
    }

    private fun Flow<AccountEvent>.handleLoginClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoginClicked>()
            .flatMapLatest {
                flow {
                    emit(AccountState(AccountLoadingState.Loading) to null)
                    emit(
                        try {
                            auth.signInWithEmailAndPassword(it.email, it.password).await()
                            loginUseCase.login()
                                .fold(
                                    mapError = {
                                        Timber.e(it.toString())
                                        AccountState(AccountLoadingState.Loading) to AccountEffect.ShowLoginNetworkError()
                                    },
                                    mapSuccess = {
                                        AccountState(AccountLoadingState.Success(it)) to null
                                    }
                                )
                        } catch (exception: Exception) {
                            AccountState(AccountLoadingState.Success(User.Guest())) to AccountEffect.ShowLoginNetworkError()
                        }
                    )
                }
            }
    }

    private fun Flow<AccountEvent>.handleRegisterClicked(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.RegisterClicked>()
            .flatMapLatest {
                flow {
                    emit(AccountState(AccountLoadingState.Loading) to null)
                    emit(
                        try {
                            auth.signOut()
                            auth.createUserWithEmailAndPassword(it.email, it.password).await()
                            registerUseCase.register()
                                .fold(
                                    mapError = {
                                        Timber.e(it.toString())
                                        AccountState(AccountLoadingState.Loading) to AccountEffect.ShowLoginNetworkError()
                                    },
                                    mapSuccess = {
                                        AccountState(AccountLoadingState.Success(it)) to null
                                    }
                                )
                        } catch (exception: Exception) {
                            Timber.e(exception)
                            AccountState(AccountLoadingState.Success(User.Guest())) to AccountEffect.ShowLoginNetworkError()
                        }
                    )
                }
            }
    }
}
