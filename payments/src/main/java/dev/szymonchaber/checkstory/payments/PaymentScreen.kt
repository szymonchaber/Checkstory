package dev.szymonchaber.checkstory.payments

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.payments.model.PaymentEvent
import dev.szymonchaber.checkstory.payments.model.PaymentState

@Composable
@Destination(route = "payment_screen", start = true)
fun PaymentScreen(navigator: DestinationsNavigator) {
//    trackScreenName("payment")
    val viewModel = hiltViewModel<PaymentViewModel>()
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
        bottomBar = {
            val context = LocalContext.current
            Box(Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp)
                        .align(Alignment.Center),
                    onClick = { viewModel.onEvent(PaymentEvent.BuyClicked(context.getActivity()!!)) }
                ) {
                    val price = "$8.99"
                    Text(text = "Buy pro for $price")
                }
            }
        }
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
            text = "Unlock Checkstory Pro",
            style = MaterialTheme.typography.h5
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
            text = "By upgrading, you get access to the full package:"
        )
        Features()
        when (val loadingState = state.paymentLoadingState) {

            is PaymentState.PaymentLoadingState.Loading -> {

            }
            is PaymentState.PaymentLoadingState.Success -> {
                PaymentsPlans(
                    loadingState.plans, loadingState.selectedPlan
                ) {
                    viewModel.onEvent(PaymentEvent.PlanSelected(it))
                }
                Text(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    text = loadingState.result
                )
            }
            PaymentState.PaymentLoadingState.Error -> TODO()
        }
    }
}

@Composable
private fun Features() {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FeatureLine("Ads-free experience")
        FeatureLine("Unlimited templates")
        FeatureLine("Unlimited reminders")
        FeatureLine("Unlimited history")
        FeatureLine("Synchronization with the web app (soon)")
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