package dev.szymonchaber.checkstory.account.session

import android.widget.Toast
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import dev.szymonchaber.checkstory.account.ConfirmLogoutDialog

@Composable
fun SessionHandler() {
    val viewModel = hiltViewModel<SessionHandlerViewModel>()
    val state = viewModel.state.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                SessionHandlerEffect.ShowLogoutSuccess -> {
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (state.value.showSessionExpiredDialog) {
        SessionExpiredDialog(viewModel::onEvent)
    }
    if (state.value.showUnsynchronizedDataDialog) {
        ConfirmLogoutDialog(
            onConfirmClicked = {
                viewModel.onEvent(SessionHandlerEvent.LogoutDespiteUnsynchronizedDataClicked)
            },
            onDismiss = {
                viewModel.onEvent(SessionHandlerEvent.LogoutCancelled)
            }
        )
    }
}

@Composable
private fun SessionExpiredDialog(onEvent: (SessionHandlerEvent) -> Unit) {
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
                    onEvent(SessionHandlerEvent.LogoutClicked)
                }) {
                    Text(text = "Logout")
                }
            },
            onDismissRequest = {},
        )
    }
}
