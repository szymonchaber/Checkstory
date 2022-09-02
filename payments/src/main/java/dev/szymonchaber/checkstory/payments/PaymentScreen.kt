package dev.szymonchaber.checkstory.payments

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.design.views.LoadingView
import dev.szymonchaber.checkstory.payments.model.PaymentEffect
import dev.szymonchaber.checkstory.payments.model.PaymentEvent
import dev.szymonchaber.checkstory.payments.model.PaymentState
import kotlinx.coroutines.launch

@Composable
@Destination(route = "payment_screen", start = true)
fun PaymentScreen(navigator: DestinationsNavigator) {
//    trackScreenName("payment")
    val viewModel = hiltViewModel<PaymentViewModel>()
    val state by viewModel.state.collectAsState(initial = PaymentState.initial)
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val effect by viewModel.effect.collectAsState(initial = null)

    val somethingWentWrongText = stringResource(id = R.string.something_went_wrong)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is PaymentEffect.PaymentError -> {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = somethingWentWrongText) // TODO add "you were not charged" to the message
                }
            }
            is PaymentEffect.ExitPaymentScreen -> {
                navigator.popBackStack()
            }
            null -> Unit
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
            PaymentView(viewModel)
        },
        scaffoldState = scaffoldState,
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                when (val loadingState = state.paymentLoadingState) {
                    is PaymentState.PaymentLoadingState.Success -> SubscribeBottomSection(loadingState, viewModel)
                    is PaymentState.PaymentLoadingState.Paid -> PaidBottomSection(viewModel)
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
            Text(text = stringResource(id = R.string.subscribe_to_checkstory_pro))
        }
    }
}

@Composable
fun ColumnScope.PaidBottomSection(viewModel: PaymentViewModel) {
    MainPaymentButton({ viewModel.onEvent(PaymentEvent.ContinueClicked) }) {
        Text(text = stringResource(id = R.string.payment_continue))
    }
}

@Composable
private fun ColumnScope.MainPaymentButton(
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp)
            .align(Alignment.CenterHorizontally),
        onClick = onClick, content = content
    )
}

@Composable
fun PaymentView(viewModel: PaymentViewModel) {
    val state by viewModel.state.collectAsState(initial = PaymentState.initial)
    Column {
        Text(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            text = stringResource(id = R.string.unlock_checkstory_pro),
            style = MaterialTheme.typography.h5
        )
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
                SubscriptionSuccessfulView()
            }
        }
    }
}

@Composable
private fun Features() {
    Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeatureLine(stringResource(R.string.ads_free_experience))
        FeatureLine(stringResource(R.string.unlimited_templates))
        FeatureLine(stringResource(R.string.unlimited_reminders))
        FeatureLine(stringResource(R.string.unlimited_history))
        FeatureLine(stringResource(R.string.synchronization_with_the_web_app_soon))
    }
}

@Composable
fun PaymentsPlans(
    subscriptionPlans: SubscriptionPlans,
    selectedPlan: SubscriptionPlan?,
    onPlanSelected: (SubscriptionPlan) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        with(subscriptionPlans) {
            SubscriptionPlanView(monthly, monthly == selectedPlan, onPlanSelected)
            SubscriptionPlanView(yearly, yearly == selectedPlan, onPlanSelected)
            SubscriptionPlanView(quarterly, quarterly == selectedPlan, onPlanSelected)
        }
    }
}

fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Composable
fun FeatureLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Image(
            modifier = Modifier.align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.ic_check),
            contentDescription = null
        )
        Text(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            text = text,
        )
    }
}

enum class PlanDuration {

    MONTHLY, QUARTERLY, YEARLY
}

data class SubscriptionPlans(
    val monthly: SubscriptionPlan,
    val quarterly: SubscriptionPlan,
    val yearly: SubscriptionPlan
)

data class SubscriptionPlan(
    val productDetails: ProductDetails,
    val offerToken: String,
    val planDuration: PlanDuration,
    val price: String,
    val pricePerMonth: String
)

@Preview
@Composable
fun SubscriptionSuccessfulView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush
                        .verticalGradient(
                            colors = listOf(
                                Color(0xFFD6CFFC),
                                Color(0xFFFDF9D8)
                            ),
                        )
                )
                .padding(30.dp)
        ) {
            Text(
                color = MaterialTheme.colors.onSecondary,
                fontWeight = FontWeight.Medium,
                text = stringResource(id = R.string.subscription_successful_thanks_for_support)
            )
        }
    }
}
