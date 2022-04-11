package dev.szymonchaber.checkstory.navigation

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import dev.szymonchaber.checkstory.checklist.catalog.destinations.ChecklistCatalogScreenDestination
import dev.szymonchaber.checkstory.checklist.fill.destinations.FillChecklistScreenDestination
import dev.szymonchaber.checkstory.checklist.history.destinations.ChecklistHistoryScreenDestination
import dev.szymonchaber.checkstory.checklist.template.destinations.EditTemplateScreenDestination

object NavGraph : NavGraphSpec {

    override val route = "root"

    override val startRoute = ChecklistCatalogScreenDestination

    override val destinationsByRoute = listOf<DestinationSpec<*>>(
        ChecklistCatalogScreenDestination,
        FillChecklistScreenDestination,
        EditTemplateScreenDestination,
        ChecklistHistoryScreenDestination
    ).associateBy { it.route }
}
