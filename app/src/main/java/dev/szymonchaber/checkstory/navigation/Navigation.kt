package dev.szymonchaber.checkstory.navigation

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.DestinationsNavHost

@Composable
fun Navigation() {
    DestinationsNavHost(navGraph = NavGraph)
}
