package dev.szymonchaber.checkstory.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.szymonchaber.checkstory.MainScreen
import dev.szymonchaber.checkstory.checklist.fill.FillChecklistScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CheckstoryScreens.HomeScreen.route) {
        composable(route = CheckstoryScreens.HomeScreen.route) {
            MainScreen(navController = navController)
        }
        composable(route = CheckstoryScreens.DetailsScreen.route) {
            FillChecklistScreen(hiltViewModel(), navController = navController)
        }
    }
}
