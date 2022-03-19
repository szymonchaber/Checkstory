package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEffect
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogLoadingState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogViewModel
import dev.szymonchaber.checkstory.navigation.CheckstoryScreens

@Composable
fun ChecklistCatalogScreen(viewModel: ChecklistCatalogViewModel, navController: NavHostController) {
    Column(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Checklists",
            style = MaterialTheme.typography.h5
        )
        val state by viewModel.state.collectAsState(initial = ChecklistCatalogState.initial)

        val effect by viewModel.effect.collectAsState(initial = null)
        LaunchedEffect(effect) {
            when (effect) {
                is ChecklistCatalogEffect.CreateAndNavigateToChecklist -> {
                    navController.navigate(CheckstoryScreens.DetailsScreen.destination((effect as ChecklistCatalogEffect.CreateAndNavigateToChecklist).basedOn))
                }
                null -> Unit
            }
        }
        when (val loadingState = state.loadingState) {
            ChecklistCatalogLoadingState.Loading -> {
                Text(text = "Loading")
            }
            is ChecklistCatalogLoadingState.Success -> {
                loadingState.checklistTemplates.forEach {
                    ChecklistTemplateView(it, viewModel::onEvent)
                }
            }
        }
    }
}
