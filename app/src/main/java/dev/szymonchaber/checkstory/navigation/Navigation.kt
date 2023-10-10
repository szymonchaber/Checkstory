package dev.szymonchaber.checkstory.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.scope.resultRecipient
import dev.szymonchaber.checkstory.account.destinations.AccountScreenDestination
import dev.szymonchaber.checkstory.payments.PaymentScreen
import dev.szymonchaber.checkstory.payments.destinations.PaymentScreenDestination

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun Navigation() {
    val navHostEngine = rememberAnimatedNavHostEngine(
        rootDefaultAnimations = RootNavGraphDefaultAnimations(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
                )
            }
        )
    )
    DestinationsNavHost(
        navGraph = NavGraph,
        engine = navHostEngine
    ) {
        composable(PaymentScreenDestination) {
            PaymentScreen(
                navigator = this.destinationsNavigator,
                registrationResultRecipient = resultRecipient<AccountScreenDestination, Boolean>()
            )
        }
    }
}
