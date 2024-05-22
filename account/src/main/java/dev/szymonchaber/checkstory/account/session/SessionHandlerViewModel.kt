package dev.szymonchaber.checkstory.account.session

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.fold
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import dev.szymonchaber.checkstory.domain.usecase.LoginUseCase
import dev.szymonchaber.checkstory.domain.usecase.LogoutResult
import dev.szymonchaber.checkstory.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import dev.szymonchaber.checkstory.account.session.SessionHandlerEffect as Effect
import dev.szymonchaber.checkstory.account.session.SessionHandlerEvent as Event
import dev.szymonchaber.checkstory.account.session.SessionHandlerState as State

@HiltViewModel
internal class SessionHandlerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val logoutUseCase: LogoutUseCase,
    private val loginUseCase: LoginUseCase,
) : BaseViewModel<Event, State, Effect>(State()) {

    init {
        viewModelScope.launch {
            userRepository.isFirebaseLoggedInFlow()
                .collectLatest { isFirebaseLoggedIn ->
                    onEvent(Event.FirebaseLoginStateChanged(isFirebaseLoggedIn))
                }
        }
    }

    override fun buildMviFlow(eventFlow: Flow<Event>): Flow<Pair<State?, Effect?>> {
        return merge(
            eventFlow.handleLoginStateChanged(),
            eventFlow.handleLoginClicked(),
            eventFlow.handleLogoutClicked(),
            eventFlow.handleFirebaseResult(),
            eventFlow.handleLogoutCancelled(),
            eventFlow.handleLogoutDespiteUnsynchronizedDataClicked(),
            eventFlow.handleTryAgainClicked()
        )
    }

    private fun Flow<Event>.handleLoginStateChanged(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.FirebaseLoginStateChanged>()
            .map { (firebaseLoggedIn) ->
                if (state.value.fixOngoing) {
                    null to null
                } else {
                    State(
                        showSessionExpiredDialog = userRepository.getCurrentUser().isLoggedIn && !firebaseLoggedIn,
                        fixOngoing = true
                    ) to null
                }
            }
    }

    private fun Flow<Event>.handleLoginClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.LoginClicked>()
            .map {
                null to Effect.LaunchFirebaseAuth((userRepository.getCurrentUser() as? User.LoggedIn)?.email)
            }
    }

    private fun Flow<Event>.handleTryAgainClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.TryAgainClicked>()
            .map {
                _state.value.copy(showAccountMismatchDialog = false) to Effect.LaunchFirebaseAuth((userRepository.getCurrentUser() as? User.LoggedIn)?.email)
            }
    }

    private fun Flow<Event>.handleFirebaseResult(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.FirebaseAuthResultReceived>()
            .flatMapLatest { (idpResponse) ->
                flow {
                    if (idpResponse.error != null) {
                        Timber.e(idpResponse.error)
                        emit(null to Effect.ShowUnknownError)
                    } else {
                        emit(handleLoginSuccess())
                    }
                }
            }
    }

    private suspend fun handleLoginSuccess(): Pair<State?, Effect?> {
        // check current user vs newly logged user id
        if (!areAccountIdsMatching()) {
            FirebaseAuth.getInstance().signOut()
            return state.value.copy(showAccountMismatchDialog = true) to null
        }
        return loginUseCase.login()
            .fold(
                mapError = {
                    Timber.e(it.toString())
                    null to Effect.ShowUnknownError
                },
                mapSuccess = {
                    State() to Effect.ShowLoginSuccessful(it.email)
                }
            )
    }

    private suspend fun areAccountIdsMatching(): Boolean {
        val currentUser = userRepository.getCurrentUser()
        FirebaseAuth.getInstance().currentUser?.uid?.let { firebaseUserId ->
            if (currentUser is User.LoggedIn && currentUser.id == firebaseUserId) {
                return true
            }
        }
        return false
    }

    private fun Flow<Event>.handleLogoutClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.LogoutClicked>()
            .map {
                when (logoutUseCase.logoutSafely()) {
                    LogoutResult.Done -> {
                        State() to Effect.ShowLogoutSuccess
                    }

                    LogoutResult.UnsynchronizedCommandsPresent -> {
                        state.value.copy(showUnsynchronizedDataDialog = true) to null
                    }
                }
            }
    }

    private fun Flow<Event>.handleLogoutDespiteUnsynchronizedDataClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.LogoutDespiteUnsynchronizedDataClicked>()
            .map {
                logoutUseCase.logoutIgnoringUnsynchronizedData()
                State() to null
            }
    }

    private fun Flow<Event>.handleLogoutCancelled(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.LogoutCancelled>()
            .map {
                state.value.copy(showUnsynchronizedDataDialog = false) to null
            }
    }
}
