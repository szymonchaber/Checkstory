package dev.szymonchaber.checkstory.account.session

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SessionHandler(modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<SessionHandlerViewModel>()
    val state = viewModel.state.collectAsState()
    if (state.value.showSessionExpiredDialog) {
        SessionExpiredDialog(modifier)
    }
}

@Composable
private fun SessionExpiredDialog(modifier: Modifier) {
    var showDialog by remember {
        mutableStateOf(true)
    }
    if (showDialog) {
        AlertDialog(
            title = {
                Text(text = "Session expired!")
            },
            text = {
                Text(text = "We need you to login again, otherwise your local data will be deleted")
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                }) {
                    Text(text = "Login")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // TODO remove this option
                    showDialog = false
                }) {
                    Text(text = "Logout")
                }
            },
            onDismissRequest = {},
        )
    }
}

@HiltViewModel
internal class SessionHandlerViewModel @Inject constructor(
    private val userRepository: UserRepository
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
            eventFlow.handleLoginStateChanged()
        )
    }

    private fun Flow<SessionHandlerEvent>.handleLoginStateChanged(): Flow<Pair<SessionHandlerState?, SessionHandlerEffect?>> {
        return filterIsInstance<SessionHandlerEvent.FirebaseLoginStateChanged>()
            .map {
                SessionHandlerState(showSessionExpiredDialog = userRepository.getCurrentUser().isLoggedIn && !it.firebaseLoggedIn) to null
            }
    }
}

internal class SessionHandlerState(
    val showSessionExpiredDialog: Boolean = false
)

internal sealed interface SessionHandlerEvent {

    data class FirebaseLoginStateChanged(val firebaseLoggedIn: Boolean) : SessionHandlerEvent
}

internal sealed interface SessionHandlerEffect
