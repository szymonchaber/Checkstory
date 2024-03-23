package dev.szymonchaber.checkstory.account

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.navigation.Routes
import dev.szymonchaber.checkstory.design.R as DesignR

@Destination(
    route = "account_screen",
    start = true,
)
@Composable
fun AccountScreen(
    navigator: ResultBackNavigator<Boolean>,
    destinationsNavigator: DestinationsNavigator,
    triggerPartialRegistration: Boolean = false,
    triggerSignIn: Boolean = false,
) {
    trackScreenName("account")
    val viewModel = hiltViewModel<AccountViewModel>()

    val firebaseAuthLauncher = rememberLauncherForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
        val idpResponse = it.idpResponse
        if (it.resultCode == Activity.RESULT_OK && idpResponse != null) {
            viewModel.onEvent(AccountEvent.FirebaseAuthResultReceived(idpResponse))
        } else {
            viewModel.onEvent(AccountEvent.FirebaseAuthFlowCancelled)
        }
    }

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val termsOfServiceUrl = stringResource(DesignR.string.terms_of_service_url)
    val privacyPolicyUrl = stringResource(DesignR.string.privacy_policy_url)
    LaunchedEffect(Unit) {
        viewModel.effect
            .collect { effect ->
                when (effect) {
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
                                        .setAllowNewAccounts(effect.allowNewAccounts)
                                        .build()
                                )
                            )
                            .setIsSmartLockEnabled(
                                /* enableCredentials = */ false,
                                /* enableHints = */ true
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
                        navigator.navigateBack(result = effect.isSuccess)
                    }

                    AccountEffect.NavigateToPurchaseScreen -> {
                        destinationsNavigator.navigate(Routes.paymentScreen())
                    }

                    AccountEffect.NavigateToSubscriptionManagement -> {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, "https://play.google.com/store/account/subscriptions".toUri())
                        )
                    }
                }
            }
    }

    LaunchedEffect(triggerPartialRegistration, triggerSignIn) {
        when {
            triggerPartialRegistration -> {
                viewModel.onEvent(AccountEvent.TriggerPartialRegistration)
            }

            triggerSignIn -> {
                viewModel.onEvent(AccountEvent.TriggerSignIn)
            }

            else -> {
                viewModel.onEvent(AccountEvent.LoadAccount)
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
    when (accountState.user) {
        is User.Guest -> {
            GuestContent(onEvent)
        }

        is User.LoggedIn -> {
            LoggedInContent(accountState.user, onEvent)
        }
    }
}

@Composable
private fun GuestContent(onEvent: (AccountEvent) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .padding(16.dp)
        ) {
            SectionHeader("Account")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Just starting out?")
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    onEvent(AccountEvent.SignUpClicked)
                }
            ) {
                Text(text = "Sign up for Checkstory SERIOUS")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text("Continue ")
                Text("right", fontStyle = FontStyle.Italic)
                Text(" where you left off")
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(onClick = { onEvent(AccountEvent.LoginClicked) }) {
                Text(text = "Login")
            }
        }
    }
}

@Composable
private fun LoggedInContent(user: User.LoggedIn, onEvent: (AccountEvent) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .padding(16.dp),
        ) {
            SectionHeader("Account")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Logged in")
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(onClick = {
                onEvent(AccountEvent.LogoutClicked)
            }) {
                Text("Logout")
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(text = "Subscription")
            Spacer(modifier = Modifier.height(8.dp))

            when (user.tier) {
                Tier.FREE -> {
                    Text("You are a Checkstory Begin user")
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = {
                        onEvent(AccountEvent.UpgradeClicked)
                    }) {
                        Text("Get SERIOUS")
                    }
                }

                Tier.PAID -> {
                    Text("You are a SERIOUS user. Thank you!")
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(onClick = {
                        onEvent(AccountEvent.ManageSubscriptionsClicked)
                    }) {
                        Text("Manage subscriptions")
                    }
                }
            }
            Spacer(modifier = Modifier.height(64.dp))
            SectionHeader(text = "Eternal consequences")
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                onClick = {
                    onEvent(AccountEvent.DeleteAccountClicked)
                }) {
                Text("Delete account (forever)")
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold)
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
