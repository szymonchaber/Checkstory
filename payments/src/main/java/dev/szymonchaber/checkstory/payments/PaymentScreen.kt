package dev.szymonchaber.checkstory.payments

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.generated.payments.destinations.PaymentScreenDestination
import com.ramcosta.composedestinations.generated.payments.destinations.PaymentSuccessScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.OpenResultRecipient
import com.ramcosta.composedestinations.spec.Direction
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.views.LoadingView
import dev.szymonchaber.checkstory.navigation.Routes
import dev.szymonchaber.checkstory.payments.billing.PlanDuration
import dev.szymonchaber.checkstory.payments.components.Features
import dev.szymonchaber.checkstory.payments.components.MainPaymentButton
import dev.szymonchaber.checkstory.payments.components.PaymentsPlans
import dev.szymonchaber.checkstory.payments.model.PaymentEffect
import dev.szymonchaber.checkstory.payments.model.PaymentEvent
import dev.szymonchaber.checkstory.payments.model.PaymentState
import kotlinx.coroutines.launch

@Composable
@Destination<PaymentGraph>(
    route = "payment_screen",
    start = true,
    deepLinks = [
        DeepLink(
            uriPattern = "app://checkstory/upgrade"
        ),
    ]
)
fun PaymentScreen(
    navigator: DestinationsNavigator,
    registrationResultRecipient: OpenResultRecipient<Boolean>
) {
    trackScreenName("upgrade_to_pro")
    val viewModel = hiltViewModel<PaymentViewModel>()
    val state by viewModel.state.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    registrationResultRecipient.onNavResult { registeredResult ->
        when (registeredResult) {
            is NavResult.Canceled -> {
                viewModel.onEvent(PaymentEvent.RegistrationCancelled)
            }

            is NavResult.Value -> {
                if (registeredResult.value) {
                    viewModel.onEvent(PaymentEvent.RegistrationSuccess(context.getActivity()!!))
                } else {
                    viewModel.onEvent(PaymentEvent.RegistrationCancelled)
                }
            }
        }
    }

    val somethingWentWrongText = stringResource(id = R.string.something_went_wrong)
    val user = ActiveUser.current
    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is PaymentEffect.PaymentError -> {
                    coroutineScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = somethingWentWrongText)
                        // TODO add "you were not charged" to the message
                    }
                }

                is PaymentEffect.ExitPaymentScreen -> {
                    navigator.popBackStack()
                }

                is PaymentEffect.NavigateToRegistration -> {
                    if (user.isLoggedIn) {
                        viewModel.onEvent(PaymentEvent.RegistrationSuccess(context.getActivity()!!))
                    } else {
                        navigator.navigate(Routes.accountScreen(triggerPartialRegistration = true))
                    }
                }

                is PaymentEffect.NavigateToPaymentSuccess -> {
                    navigator.navigate(Direction(PaymentSuccessScreenDestination.route)) {
                        popUpTo(Direction(PaymentScreenDestination.route)) { inclusive = true }
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.upgrade))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.navigateUp()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp
            )
        }, content = {
            Box(modifier = Modifier.padding(it)) {
                PaymentView(viewModel)
            }
        },
        scaffoldState = scaffoldState,
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                when (val loadingState = state.paymentLoadingState) {
                    is PaymentState.PaymentLoadingState.Success -> SubscribeBottomSection(loadingState, viewModel)
                    else -> Unit
                }
            }
        }
    )
}

@Composable
private fun ColumnScope.SubscribeBottomSection(
    success: PaymentState.PaymentLoadingState.Success,
    viewModel: PaymentViewModel,
) {
    val context = LocalContext.current

    val billingFrequency = when (success.selectedPlan.planDuration) {
        PlanDuration.MONTHLY -> R.string.price_billed_monthly
        PlanDuration.QUARTERLY -> R.string.price_billed_quarterly
        PlanDuration.YEARLY -> R.string.price_billed_yearly
    }
    Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = stringResource(billingFrequency, success.selectedPlan.price),
        style = MaterialTheme.typography.caption
    )

    MainPaymentButton({ viewModel.onEvent(PaymentEvent.BuyClicked(context.getActivity()!!)) }) {
        if (success.paymentInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colors.secondary,
                strokeWidth = 1.5.dp
            )
        } else {
            if (ActiveUser.current.isLoggedIn) {
                Text(text = "Upgrade to PRO")
            } else {
                Text(text = "Register & upgrade to PRO")
            }
        }
    }
}

@Composable
internal fun PaymentView(viewModel: PaymentViewModel) {
    val state by viewModel.state.collectAsState()
    Column {
        Text(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp),
            text = stringResource(id = R.string.get_access_to_full_package)
        )
        Features()
        when (val loadingState = state.paymentLoadingState) {
            is PaymentState.PaymentLoadingState.Loading -> {
                LoadingView()
            }

            is PaymentState.PaymentLoadingState.Success -> {
                PaymentsPlans(
                    loadingState.plans, loadingState.selectedPlan
                ) {
                    viewModel.onEvent(PaymentEvent.PlanSelected(it))
                }
            }

            PaymentState.PaymentLoadingState.LoadingError -> {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp), text = stringResource(id = R.string.could_not_load_subscription_plans)
                )
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { viewModel.onEvent(PaymentEvent.LoadSubscriptionPlans) }) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }

            PaymentState.PaymentLoadingState.Paid -> {
//                SubscriptionSuccessScreen()
            }
        }
    }
}

fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
