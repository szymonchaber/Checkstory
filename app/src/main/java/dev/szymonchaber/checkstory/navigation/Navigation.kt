package dev.szymonchaber.checkstory.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
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
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument(CheckstoryScreens.DetailsScreen.sourceChecklistTemplateIdArg) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            FillChecklistScreen(
                hiltViewModel(),
                navController,
                it.arguments?.getLong(
                    CheckstoryScreens.DetailsScreen.checklistIdArg,
                    -1L
                )
                    ?.takeUnless { id ->
                        id == -1L
                    }
                    ?.let(::ChecklistId),
                it.arguments?.getLong(
                    CheckstoryScreens.DetailsScreen.sourceChecklistTemplateIdArg,
                    -1L
                )
                    ?.takeUnless { id ->
                        id == -1L
                    }
                    ?.let(::ChecklistTemplateId)
            )
        }
        composable(
            route = CheckstoryScreens.EditTemplateScreen.route,
            arguments = listOf(
                navArgument(CheckstoryScreens.EditTemplateScreen.templateIdArg) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            )
        ) {
            EditTemplateScreen(
                hiltViewModel(),
                navController,
                it.arguments?.getLong(CheckstoryScreens.EditTemplateScreen.templateIdArg, -1L)
                    ?.takeUnless { id ->
                        id == -1L
                    }
                    ?.let(::ChecklistTemplateId)
            )
        }
    }
}
