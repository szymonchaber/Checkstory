package dev.szymonchaber.checkstory.account

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.User

@Destination(
    route = "account_screen",
    start = true,
)
@Composable
fun AccountScreen(
    navigator: DestinationsNavigator,
) {
    trackScreenName("account")
    val viewModel = hiltViewModel<AccountViewModel>()
    viewModel.onEvent(AccountEvent.LoadAccount)

    val state = viewModel.state.collectAsState(initial = AccountState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    val context = LocalContext.current
    LaunchedEffect(effect) {
        when (val value = effect) {
            is AccountEffect.ShowLoginNetworkError -> {
                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }

            is AccountEffect.ShowDataNotSynchronized -> {
                Toast.makeText(context, "There are some unsynchronized changes. Logout anyway?", Toast.LENGTH_SHORT)
                    .show()
            }

            null -> Unit
        }
    }

    FillChecklistScaffold(viewModel, state, navigator)
}

@Composable
private fun FillChecklistScaffold(
    viewModel: AccountViewModel,
    state: State<AccountState>,
    navigator: DestinationsNavigator
) {
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Account")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigator.navigateUp()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp,
            )
        },
        content = {
            when (val loadingState = state.value.accountLoadingState) {
                AccountLoadingState.Loading -> {
                    FullSizeLoadingView()
                }

                is AccountLoadingState.Success -> {
                    AccountView(loadingState, viewModel::onEvent)
                }
            }
        },
    )
}

@Composable
fun AccountView(accountState: AccountLoadingState.Success, onEvent: (AccountEvent) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text(
            when (accountState.user) {
                is User.Guest -> "Not logged in"
                is User.LoggedIn -> "Logged in"
            }
        )
        when (accountState.user) {
            is User.Guest -> {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                TextField(
                    value = email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    onValueChange = { email = it })
                TextField(
                    value = password,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    onValueChange = { password = it })
                RegisterButton {
                    onEvent(AccountEvent.RegisterClicked(email, password))
                }
                LoginButton {
                    onEvent(AccountEvent.LoginClicked(email, password))
                }
            }
            is User.LoggedIn -> {
                LogoutButton(onEvent)
            }
        }
    }
}

@Composable
fun LogoutButton(onEvent: (AccountEvent) -> Unit) {
    Button(onClick = {
        onEvent(AccountEvent.LogoutClicked)
    }) {
        Text("Logout")
    }
}

@Composable
private fun LoginButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Login with Firebase")
    }
}

@Composable
private fun RegisterButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Register with Firebase")
    }
}
