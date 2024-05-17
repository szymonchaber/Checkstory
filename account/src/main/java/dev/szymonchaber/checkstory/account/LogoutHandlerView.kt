package dev.szymonchaber.checkstory.account

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
                Text(text = "We need you to login again, otherwise your data will be deleted")
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
                    Text(text = "Cancel")
                }
            },
            onDismissRequest = {},
        )
    }
}

@HiltViewModel
internal class SessionHandlerViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val state = userRepository.isFirebaseLoggedInFlow()
        .map { isFirebaseLoggedIn ->
            SessionHandlerState(showSessionExpiredDialog = userRepository.getCurrentUser().isLoggedIn && !isFirebaseLoggedIn)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SessionHandlerState())
}

internal class SessionHandlerState(
    val showSessionExpiredDialog: Boolean = false
)
