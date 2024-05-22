package dev.szymonchaber.checkstory.account.session

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import dev.szymonchaber.checkstory.account.ConfirmLogoutDialog
import dev.szymonchaber.checkstory.account.firebase.createFirebaseSignInIntent
import dev.szymonchaber.checkstory.account.showLoggedInToast
import dev.szymonchaber.checkstory.design.R

@Composable
fun SessionHandler() {
    val viewModel = hiltViewModel<SessionHandlerViewModel>()
    val state = viewModel.state.collectAsState()

    val firebaseAuthLauncher = rememberLauncherForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
        val idpResponse = it.idpResponse
        if (it.resultCode == Activity.RESULT_OK && idpResponse != null) {
            viewModel.onEvent(SessionHandlerEvent.FirebaseAuthResultReceived(idpResponse))
        }
    }

    val context = LocalContext.current
    val termsOfServiceUrl = stringResource(R.string.terms_of_service_url)
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                SessionHandlerEffect.ShowLogoutSuccess -> {
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                }

                is SessionHandlerEffect.LaunchFirebaseAuth -> {
                    val signInIntent = createFirebaseSignInIntent(
                        termsOfServiceUrl = termsOfServiceUrl,
                        privacyPolicyUrl = privacyPolicyUrl,
                        allowNewAccounts = false,
                        defaultEmail = it.defaultEmail
                    )
                    firebaseAuthLauncher.launch(signInIntent)
                }

                is SessionHandlerEffect.ShowLoginSuccessful -> {
                    showLoggedInToast(it.email, context)
                }

                SessionHandlerEffect.ShowUnknownError -> {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
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
    if (state.value.showAccountMismatchDialog) {
        AccountMismatchDialog(viewModel)
    }
}

@Composable
private fun AccountMismatchDialog(viewModel: SessionHandlerViewModel) {
    AlertDialog(
        title = {
            Text(text = "Account mismatch")
        },
        text = {
            Text(text = "You are trying to login with a different account than the one you were previously logged in with. Please try again.")
        },
        confirmButton = {
            Button(onClick = {
                viewModel.onEvent(SessionHandlerEvent.TryAgainClicked)
            }) {
                Text(text = "Try again")
            }
        },
        dismissButton = {
            Button(onClick = {
                viewModel.onEvent(SessionHandlerEvent.LogoutClicked)
            }) {
                Text(text = "Logout")
            }
        },
        onDismissRequest = {}
    )
}

@Composable
private fun SessionExpiredDialog(onEvent: (SessionHandlerEvent) -> Unit) {
    AlertDialog(
        title = {
            Text(text = "Session expired!")
        },
        text = {
            Text(text = "You have to login again, otherwise your local data will be deleted")
        },
        confirmButton = {
            Button(onClick = {
                onEvent(SessionHandlerEvent.LoginClicked)
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
