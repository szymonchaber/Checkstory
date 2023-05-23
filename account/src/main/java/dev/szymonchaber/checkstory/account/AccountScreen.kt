package dev.szymonchaber.checkstory.account

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.User
import timber.log.Timber

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
    LaunchedEffect(effect) {
        when (val value = effect) {
            AccountEffect.ShowLoginNetworkError -> {
                Timber.e("Login network error")
            }

            AccountEffect.ShowDataNotSynchronized -> {
                Timber.d("Data not synchronized")
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
                User.Guest -> "Not logged in"
                is User.LoggedIn -> "Logged in"
            }
        )
        when (accountState.user) {
            User.Guest -> {
                LoginButton(onEvent)
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
private fun LoginButton(onEvent: (AccountEvent) -> Unit) {
    val context = LocalContext.current
    val auth = remember {
        Firebase.auth
    }
    Button(onClick = {
        auth.signInWithEmailAndPassword("", "")
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful) {
                    Timber.d("createUserWithEmail: success")
                    onEvent(AccountEvent.LoginSuccess)
                } else {
                    Timber.e(task.exception, "createUserWithEmail: failure")
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    onEvent(AccountEvent.LoginFailed)
                }
            }
    }) {
        Text("Login with Firebase")
    }
}
