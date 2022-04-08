package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.szymonchaber.checkstory.checklist.catalog.model.*
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.navigation.CheckstoryScreens

@Composable
fun ChecklistCatalogScreen(viewModel: ChecklistCatalogViewModel, navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Checkstory")
                },
                elevation = 12.dp
            )
        }, content = {
            ChecklistCatalogView(viewModel, navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(CheckstoryScreens.EditTemplateScreen.createTemplate()) }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            }
        }
    )
}

@Composable
private fun ChecklistCatalogView(
    viewModel: ChecklistCatalogViewModel,
    navController: NavHostController
) {
    Column(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        val state by viewModel.state.collectAsState(initial = ChecklistCatalogState.initial)

        val effect by viewModel.effect.collectAsState(initial = null)
        LaunchedEffect(effect) {
            when (effect) {
                is ChecklistCatalogEffect.CreateAndNavigateToChecklist -> {
                    navController.navigate(CheckstoryScreens.DetailsScreen.createChecklist((effect as ChecklistCatalogEffect.CreateAndNavigateToChecklist).basedOn))
                }
                is ChecklistCatalogEffect.NavigateToChecklist -> {
                    navController.navigate(CheckstoryScreens.DetailsScreen.goToChecklist((effect as ChecklistCatalogEffect.NavigateToChecklist).checklistId))
                }
                null -> Unit
            }
        }
        RecentChecklistsView(state, viewModel)
        TemplatesList(state, viewModel)
    }
}

@Composable
fun RecentChecklistsView(state: ChecklistCatalogState, viewModel: ChecklistCatalogViewModel) {
    Text("Recent checklists", style = MaterialTheme.typography.h5)
    when (val loadingState = state.recentChecklistsLoadingState) {
        RecentChecklistsLoadingState.Loading -> {
            Text(text = "Loading")
        }
        is RecentChecklistsLoadingState.Success -> {
            LazyRow {
                items(loadingState.checklists) {
                    RecentChecklistItemView(it, viewModel::onEvent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecentChecklistItemView(
    checklist: Checklist,
    eventListener: (ChecklistCatalogEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(top = 16.dp, end = 8.dp)
            .widthIn(max = 160.dp),
        elevation = 4.dp,
        onClick = {
            eventListener(ChecklistCatalogEvent.RecentChecklistClicked(checklist.id))
        }
    ) {
        val notes by remember {
            mutableStateOf(checklist.notes.takeUnless(String::isBlank)?.let { "\"$it\"" } ?: "🙊")
        }
        Column(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Text(
                text = checklist.title,
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = notes,
                style = MaterialTheme.typography.subtitle1
            )
        }

    }
}

@Composable
private fun TemplatesList(
    state: ChecklistCatalogState,
    viewModel: ChecklistCatalogViewModel
) {
    Text(modifier = Modifier.padding(top = 24.dp), text = "Templates", style = MaterialTheme.typography.h5)
    when (val loadingState = state.templatesLoadingState) {
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
