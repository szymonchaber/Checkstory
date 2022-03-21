package dev.szymonchaber.checkstory.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.szymonchaber.checkstory.checklist.catalog.ChecklistCatalogScreen
import dev.szymonchaber.checkstory.checklist.fill.FillChecklistScreen
import dev.szymonchaber.checkstory.checklist.template.EditTemplateScreen
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = CheckstoryScreens.HomeScreen.route
    ) {
        composable(route = CheckstoryScreens.HomeScreen.route) {
            ChecklistCatalogScreen(hiltViewModel(), navController)
        }
        composable(
            route = CheckstoryScreens.DetailsScreen.route,
            arguments = listOf(
                navArgument(CheckstoryScreens.DetailsScreen.checklistIdArg) {
                    nullable = true
                    defaultValue = null
                },
                navArgument(CheckstoryScreens.DetailsScreen.sourceChecklistTemplateIdArg) {
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            FillChecklistScreen(
                hiltViewModel(),
                navController,
                it.arguments?.getString(CheckstoryScreens.DetailsScreen.checklistIdArg)
                    ?.let(::ChecklistId),
                it.arguments?.getString(CheckstoryScreens.DetailsScreen.sourceChecklistTemplateIdArg)
                    ?.let(::ChecklistTemplateId)
            )
        }
        composable(
            route = CheckstoryScreens.EditTemplateScreen.route,
            arguments = listOf(
                navArgument(CheckstoryScreens.EditTemplateScreen.templateIdArg) {
                    nullable = true
                    defaultValue = null
                },
            )
        ) {
            EditTemplateScreen(
                hiltViewModel(),
                navController,
                it.arguments?.getString(CheckstoryScreens.EditTemplateScreen.templateIdArg)
                    ?.let(::ChecklistTemplateId)
            )
        }
    }
}
