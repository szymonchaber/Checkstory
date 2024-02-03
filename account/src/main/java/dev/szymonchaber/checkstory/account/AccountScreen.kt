package dev.szymonchaber.checkstory.account

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.design.R as DesignR

@Destination(
    route = "account_screen",
    start = true,
)
@Composable
fun AccountScreen(
    navigator: ResultBackNavigator<Boolean>,
    triggerPartialRegistration: Boolean = false
) {
    trackScreenName("account")
    val viewModel = hiltViewModel<AccountViewModel>()
    LaunchedEffect(triggerPartialRegistration) {
        if (triggerPartialRegistration) {
            viewModel.onEvent(AccountEvent.TriggerPartialRegistration)
        } else {
            viewModel.onEvent(AccountEvent.LoadAccount)
        }
    }

    val firebaseAuthLauncher = rememberLauncherForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
        it.idpResponse?.let { identityProviderResponse ->
            (viewModel::onEvent)(AccountEvent.FirebaseResultReceived(identityProviderResponse))
        }
    }

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val termsOfServiceUrl = stringResource(DesignR.string.terms_of_service_url)
    val privacyPolicyUrl = stringResource(DesignR.string.privacy_policy_url)
    LaunchedEffect(Unit) {
        viewModel.effect
            .collect { effect ->
                when (val value = effect) {
                    is AccountEffect.ShowLoginNetworkError -> {
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }

                    is AccountEffect.ShowDataNotSynchronized -> {
                        Toast.makeText(
                            context,
                            "There are some unsynchronized changes. Logout anyway?",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    is AccountEffect.StartAuthUi -> {
                        val signInIntent = AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                listOf(
                                    AuthUI.IdpConfig.EmailBuilder()
                                        .setRequireName(false)
                                        // TODO disallow for login / split login & registration and force a purchase?
                                        .setAllowNewAccounts(true)
                                        .build()
                                )
                            )
                            .setTheme(DesignR.style.Theme_Checkstory)
                            .setTosAndPrivacyPolicyUrls(
                                termsOfServiceUrl,
                                privacyPolicyUrl,
                            )
                            .build()
                        firebaseAuthLauncher.launch(signInIntent)
                    }

                    is AccountEffect.ExitWithAuthResult -> {
                        navigator.navigateBack(result = value.isSuccess)
                    }
                }
            }
    }

    AccountScaffold(state, viewModel::onEvent) {
        navigator.navigateBack()
    }
}

@Composable
private fun AccountScaffold(
    state: AccountState,
    onEvent: (AccountEvent) -> Unit,
    onBackClicked: () -> Unit
) {
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Account")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp,
            )
        },
        content = {
            when (val loadingState = state.accountLoadingState) {
                AccountLoadingState.Loading -> {
                    FullSizeLoadingView()
                }

                is AccountLoadingState.Success -> {
                    AccountView(loadingState, onEvent)
                }
            }
        },
    )
}

@Composable
fun AccountView(
    accountState: AccountLoadingState.Success,
    onEvent: (AccountEvent) -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Text(
            when (accountState.user) {
                is User.Guest -> "Not logged in"
                is User.LoggedIn -> "Logged in"
            }
        )
        when (accountState.user) {
            is User.Guest -> {
                Button(onClick = { onEvent(AccountEvent.FirebaseLoginClicked) }) {
                    Text(text = "Login")
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

@Preview
@Composable
fun AgeEstimationPreview(
    @PreviewParameter(AccountStateProvider::class) state: AccountState,
) {
    CheckstoryTheme {
        CompositionLocalProvider(
            ActiveUser provides User.Guest(false)
        ) {
            AccountScaffold(state = state, onEvent = {}, onBackClicked = {})
        }
    }
}

private class AccountStateProvider : CollectionPreviewParameterProvider<AccountState>(
    listOf(
        AccountState(
            accountLoadingState = AccountLoadingState.Loading,
            partialAuthRequested = false
        ),
        AccountState(
            accountLoadingState = AccountLoadingState.Success(User.Guest(false)),
            partialAuthRequested = false
        ),
//        AccountState(
//            accountLoadingState = AccountLoadingState.Success(User.Guest(true)),
//            partialAuthRequested = false
//        ),
        AccountState(
            accountLoadingState = AccountLoadingState.Success(User.LoggedIn(Tier.FREE)),
            partialAuthRequested = false
        ),
        AccountState(
            accountLoadingState = AccountLoadingState.Success(User.LoggedIn(Tier.PAID)),
            partialAuthRequested = false
        ),
    ),
)
