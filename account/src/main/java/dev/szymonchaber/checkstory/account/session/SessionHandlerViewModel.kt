package dev.szymonchaber.checkstory.account.session

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.account.AccountEvent
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import dev.szymonchaber.checkstory.domain.usecase.LogoutResult
import dev.szymonchaber.checkstory.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SessionHandlerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<
        SessionHandlerEvent,
        SessionHandlerState,
        SessionHandlerEffect
        >(SessionHandlerState()) {

    init {
        viewModelScope.launch {
            userRepository.isFirebaseLoggedInFlow()
                .collectLatest { isFirebaseLoggedIn ->
                    onEvent(SessionHandlerEvent.FirebaseLoginStateChanged(isFirebaseLoggedIn))
                }
        }
    }

    override fun buildMviFlow(eventFlow: Flow<SessionHandlerEvent>): Flow<Pair<SessionHandlerState?, SessionHandlerEffect?>> {
        return merge(
            eventFlow.handleLoginStateChanged(),
            eventFlow.handleLogoutClicked(),
            eventFlow.handleLogoutCancelled(),
            eventFlow.handleLogoutDespiteUnsynchronizedDataClicked()
        )
    }

    private fun Flow<SessionHandlerEvent>.handleLoginStateChanged(): Flow<Pair<SessionHandlerState?, SessionHandlerEffect?>> {
        return filterIsInstance<SessionHandlerEvent.FirebaseLoginStateChanged>()
            .map { (firebaseLoggedIn) ->
                SessionHandlerState(showSessionExpiredDialog = userRepository.getCurrentUser().isLoggedIn && !firebaseLoggedIn) to null
            }
    }

    private fun Flow<SessionHandlerEvent>.handleLogoutClicked(): Flow<Pair<SessionHandlerState?, SessionHandlerEffect?>> {
        return filterIsInstance<SessionHandlerEvent.LogoutClicked>()
            .map {
                when (logoutUseCase.logoutSafely()) {
                    LogoutResult.Done -> {
                        SessionHandlerState() to SessionHandlerEffect.ShowLogoutSuccess
                    }

                    LogoutResult.UnsynchronizedCommandsPresent -> {
                        state.value.copy(showUnsynchronizedDataDialog = true) to null
                    }
                }
            }
    }

    private fun Flow<SessionHandlerEvent>.handleLogoutDespiteUnsynchronizedDataClicked(): Flow<Pair<SessionHandlerState?, SessionHandlerEffect?>> {
        return filterIsInstance<AccountEvent.LogoutDespiteUnsynchronizedDataClicked>()
            .map {
                logoutUseCase.logoutIgnoringUnsynchronizedData()
                SessionHandlerState() to null
            }
    }

    private fun Flow<SessionHandlerEvent>.handleLogoutCancelled(): Flow<Pair<SessionHandlerState?, SessionHandlerEffect?>> {
        return filterIsInstance<SessionHandlerEvent.LogoutCancelled>()
            .map {
                state.value.copy(showUnsynchronizedDataDialog = false) to null
            }
    }
}
