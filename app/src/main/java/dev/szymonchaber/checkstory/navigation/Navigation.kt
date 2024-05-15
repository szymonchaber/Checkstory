package dev.szymonchaber.checkstory.navigation

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.account.destinations.AccountScreenDestination
import com.ramcosta.composedestinations.generated.payments.destinations.PaymentScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.scope.resultRecipient
import dev.szymonchaber.checkstory.payments.PaymentScreen

@Composable
fun Navigation() {
    DestinationsNavHost(navGraph = NavGraphs.main) {
        composable(PaymentScreenDestination) {
            PaymentScreen(
                navigator = destinationsNavigator,
                registrationResultRecipient = resultRecipient<AccountScreenDestination, Boolean>()
            )
        }
    }
}
