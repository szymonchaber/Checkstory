package dev.szymonchaber.checkstory.navigation

sealed class CheckstoryScreens(val route: String) {

    object HomeScreen : CheckstoryScreens("home_screen")
    object DetailsScreen : CheckstoryScreens("details_screen")
}
