package dev.szymonchaber.checkstory.payments

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.payments.model.PaymentEvent
import dev.szymonchaber.checkstory.payments.model.PaymentState

@Composable
@Destination(route = "payment_screen", start = true)
fun PaymentScreen(navigator: DestinationsNavigator) {
//    trackScreenName("payment")
    val viewModel = hiltViewModel<PaymentViewModel>()
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.checkstory))
                },
                elevation = 12.dp
            )
        }, content = {
            PaymentView(viewModel)
        }
    )
}

@Composable
fun PaymentView(viewModel: PaymentViewModel) {
    val state by viewModel.state.collectAsState(initial = PaymentState.initial)

    Column {
        Text(text = state.result)
        TextButton(onClick = { viewModel.onEvent(PaymentEvent.BuyClicked) }) {
            Text(text = "Buy pro version")
        }
    }
}
