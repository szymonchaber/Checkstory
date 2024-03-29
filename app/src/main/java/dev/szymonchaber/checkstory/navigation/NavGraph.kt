package dev.szymonchaber.checkstory.navigation

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import dev.szymonchaber.checkstory.about.destinations.AboutScreenDestination
import dev.szymonchaber.checkstory.account.destinations.AccountScreenDestination
import dev.szymonchaber.checkstory.checklist.catalog.destinations.ChecklistCatalogScreenDestination
import dev.szymonchaber.checkstory.checklist.catalog.destinations.DebugScreenDestination
import dev.szymonchaber.checkstory.checklist.fill.destinations.FillChecklistScreenDestination
import dev.szymonchaber.checkstory.checklist.history.destinations.ChecklistHistoryScreenDestination
import dev.szymonchaber.checkstory.checklist.template.destinations.EditTemplateScreenDestination
import dev.szymonchaber.checkstory.onboarding.destinations.OnboardingScreenDestination
import dev.szymonchaber.checkstory.payments.destinations.PaymentScreenDestination
import dev.szymonchaber.checkstory.payments.destinations.PaymentSuccessScreenDestination

object NavGraph : NavGraphSpec {

    override val route = "root"

    override val startRoute = ChecklistCatalogScreenDestination

    override val destinationsByRoute = listOf<DestinationSpec<*>>(
        OnboardingScreenDestination,
        ChecklistCatalogScreenDestination,
        FillChecklistScreenDestination,
        EditTemplateScreenDestination,
        ChecklistHistoryScreenDestination,
        PaymentScreenDestination,
        AboutScreenDestination,
        DebugScreenDestination,
        AccountScreenDestination,
        PaymentSuccessScreenDestination
    ).associateBy { it.route }
}
