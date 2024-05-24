package dev.szymonchaber.checkstory.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.generated.about.navgraphs.AboutNavGraph
import com.ramcosta.composedestinations.generated.account.navgraphs.AccountNavGraph
import com.ramcosta.composedestinations.generated.checklistfill.navgraphs.FillChecklistNavGraph
import com.ramcosta.composedestinations.generated.checklisthistory.navgraphs.ChecklistHistoryNavGraph
import com.ramcosta.composedestinations.generated.checklisttemplate.navgraphs.EditTemplateNavGraph
import com.ramcosta.composedestinations.generated.home.navgraphs.HomeNavGraph
import com.ramcosta.composedestinations.generated.onboarding.navgraphs.OnboardingNavGraph
import com.ramcosta.composedestinations.generated.payments.navgraphs.PaymentNavGraph

@NavHostGraph(defaultTransitions = DefaultTransitions::class)
internal annotation class MainGraph {

    @ExternalNavGraph<HomeNavGraph>(start = true)
    @ExternalNavGraph<EditTemplateNavGraph>()
    @ExternalNavGraph<FillChecklistNavGraph>()
    @ExternalNavGraph<ChecklistHistoryNavGraph>()
    @ExternalNavGraph<AccountNavGraph>()
    @ExternalNavGraph<AboutNavGraph>()
    @ExternalNavGraph<PaymentNavGraph>()
    @ExternalNavGraph<OnboardingNavGraph>()
    companion object Includes
}

internal object DefaultTransitions : NavHostAnimatedDestinationStyle() {

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { 1000 },
            animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
        )
    }

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -1000 },
            animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
        )
    }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -1000 },
            animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
        )
    }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { 1000 },
            animationSpec = tween(AnimationConstants.TRANSITION_DURATION)
        )
    }
}
