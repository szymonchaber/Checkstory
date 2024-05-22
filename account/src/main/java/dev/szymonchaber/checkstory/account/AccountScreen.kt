package dev.szymonchaber.checkstory.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import dev.szymonchaber.checkstory.account.firebase.createFirebaseSignInIntent
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.navigation.Routes
import dev.szymonchaber.checkstory.design.R as DesignR

@NavGraph<ExternalModuleGraph>
annotation class AccountGraph

@Destination<AccountGraph>(
    route = "account_screen",
    start = true,
)
@Composable
fun AccountScreen(
    navigator: ResultBackNavigator<Boolean>,
    destinationsNavigator: DestinationsNavigator,
    triggerPartialRegistration: Boolean = false,
    triggerPurchaseRestoration: Boolean = false
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

    val openConfirmLogoutDialog = remember { mutableStateOf(false) }
    if (openConfirmLogoutDialog.value) {
        ConfirmLogoutDialog(onConfirmClicked = {
            viewModel.onEvent(AccountEvent.LogoutDespiteUnsynchronizedDataClicked)
            openConfirmLogoutDialog.value = false
        }, onDismiss = {
            openConfirmLogoutDialog.value = false
        })
    }

    val openConfirmDeleteAccountDialog = remember { mutableStateOf(false) }
    if (openConfirmDeleteAccountDialog.value) {
        ConfirmDeleteAccountDialog(openConfirmDeleteAccountDialog) {
            viewModel.onEvent(AccountEvent.DeleteAccountConfirmed)
            openConfirmDeleteAccountDialog.value = false
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
                        openConfirmLogoutDialog.value = true
                    }

                    is AccountEffect.StartAuthUi -> {
                        val signInIntent = createFirebaseSignInIntent(termsOfServiceUrl, privacyPolicyUrl)
                        firebaseAuthLauncher.launch(signInIntent)
                    }

                    is AccountEffect.ExitWithAuthResult -> {
                        if (effect.isSuccess) {
                            showLoggedInToast(effect.loggedInEmail, context)
                        }
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

                    AccountEffect.ShowNoPurchasesFound -> {
                        Toast.makeText(context, "No purchases to restore", Toast.LENGTH_LONG).show()
                    }

                    AccountEffect.ShowPurchaseRestored -> {
                        Toast.makeText(context, "Purchase restored!", Toast.LENGTH_LONG).show()
                    }

                    AccountEffect.ShowPurchaseRestorationFailed -> {
                        Toast.makeText(context, "Purchase restoration failed", Toast.LENGTH_LONG).show()
                    }

                    AccountEffect.ShowPurchaseAssignedToAnotherUser -> {
                        Toast.makeText(context, "Purchase already assigned to another user", Toast.LENGTH_LONG).show()
                    }

                    AccountEffect.ShowConfirmDeleteAccountDialog -> {
                        openConfirmDeleteAccountDialog.value = true
                    }

                    AccountEffect.ShowAccountDeleted -> {
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    LaunchedEffect(triggerPartialRegistration, triggerPurchaseRestoration) {
        when {
            triggerPartialRegistration -> {
                viewModel.onEvent(AccountEvent.TriggerPartialRegistration)
            }

            triggerPurchaseRestoration -> {
                viewModel.onEvent(AccountEvent.RestorePaymentClicked)
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

internal fun showLoggedInToast(email: String?, context: Context) {
    email?.ifBlank {
        null
    }?.let {
        Toast.makeText(context, "Logged in as $it", Toast.LENGTH_SHORT).show()
    } ?: run {
        Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show()
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
    Column {
        when (accountState.user) {
            is User.Guest -> {
                GuestContent(onEvent)
            }

            is User.LoggedIn -> {
                LoggedInContent(accountState.user, onEvent)
            }
        }
    }
}

@Composable
fun RestorePaymentContent(onEvent: (AccountEvent) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
    ) {
        SectionHeader("Payment restoration")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = {
            onEvent(AccountEvent.RestorePaymentClicked)
        }) {
            Text("Restore payment")
        }
    }
}

@Composable
private fun GuestContent(onEvent: (AccountEvent) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SectionHeader("Account")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Logged out")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { onEvent(AccountEvent.LoginClicked) }) {
            Text(text = "Login/Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(text = "Subscription")
        Spacer(modifier = Modifier.height(8.dp))
        Text("You are on the free tier")
        Button(
            onClick = {
                onEvent(AccountEvent.SignUpClicked)
            }
        ) {
            Text(text = "Upgrade to Checkstory Pro")
        }
        Spacer(modifier = Modifier.height(16.dp))
        RestorePaymentContent(onEvent)
    }
}

@Composable
private fun LoggedInContent(user: User.LoggedIn, onEvent: (AccountEvent) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        SectionHeader("Account")
        Spacer(modifier = Modifier.height(8.dp))
        val emailPart by remember(user) {
            derivedStateOf {
                user.email?.let {
                    ": $it"
                }.orEmpty()
            }
        }
        Text("Logged in$emailPart")
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
                Text("You are on the free tier")
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = {
                    onEvent(AccountEvent.UpgradeClicked)
                }) {
                    Text("Upgrade to Checkstory Pro")
                }
            }

            Tier.PAID -> {
                Text("You are on the Pro tier. Thank you!")
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(onClick = {
                    onEvent(AccountEvent.ManageSubscriptionsClicked)
                }) {
                    Text("Manage subscriptions")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        RestorePaymentContent(onEvent)
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

@Composable
private fun SectionHeader(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold)
}

@Preview
@Composable
fun AccountStatePreview(
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
            accountLoadingState = AccountLoadingState.Success(User.Guest(false)),
            authForPaymentRequested = false
        ),
//        AccountState(
//            accountLoadingState = AccountLoadingState.Success(User.Guest(true)),
//            partialAuthRequested = false
//        ),
        AccountState(
            accountLoadingState = AccountLoadingState.Success(User.LoggedIn("", "szymon@szymonchaber.dev", Tier.FREE)),
            authForPaymentRequested = false
        ),
        AccountState(
            accountLoadingState = AccountLoadingState.Success(User.LoggedIn("", "szymon@szymonchaber.dev", Tier.PAID)),
            authForPaymentRequested = false
        ),
    ),
)

@Composable
fun ConfirmLogoutDialog(
    onConfirmClicked: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Logout")
        },
        text = {
            Text("There are some unsynchronized changes. Logging out will discard them. Continue?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClicked
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmDeleteAccountDialog(openDialog: MutableState<Boolean>, onConfirmClicked: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            openDialog.value = false
        },
        title = {
            Text("Delete account")
        },
        text = {
            Text("Deleting your account will remove all your data. Continue?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClicked
            ) {
                Text("Delete account")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    openDialog.value = false
                }) {
                Text("Cancel")
            }
        }
    )
}
