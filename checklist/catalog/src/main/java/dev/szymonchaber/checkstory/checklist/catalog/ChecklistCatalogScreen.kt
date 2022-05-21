package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEffect
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogLoadingState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogViewModel
import dev.szymonchaber.checkstory.checklist.catalog.recent.RecentChecklistsView
import dev.szymonchaber.checkstory.checklist.fill.destinations.FillChecklistScreenDestination
import dev.szymonchaber.checkstory.checklist.history.destinations.ChecklistHistoryScreenDestination
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.LoadingView
import dev.szymonchaber.checkstory.navigation.Routes

@Composable
@Destination(route = "home_screen", start = true)
fun ChecklistCatalogScreen(navigator: DestinationsNavigator) {
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.checkstory))
                },
                elevation = 12.dp
            )
        }, content = {
            ChecklistCatalogView(hiltViewModel(), navigator)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navigator.navigate(Routes.newChecklistTemplateScreen())
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            }
        }
    )
}

@Composable
private fun ChecklistCatalogView(
    viewModel: ChecklistCatalogViewModel,
    navigator: DestinationsNavigator
) {
    val state by viewModel.state.collectAsState(initial = ChecklistCatalogState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is ChecklistCatalogEffect.CreateAndNavigateToChecklist -> {
                navigator.navigate(
                    FillChecklistScreenDestination(createChecklistFrom = value.basedOn)
                )
            }
            is ChecklistCatalogEffect.NavigateToChecklist -> {
                navigator.navigate(FillChecklistScreenDestination(checklistId = value.checklistId))
            }
            is ChecklistCatalogEffect.NavigateToTemplateEdit -> {
                navigator.navigate(Routes.editChecklistTemplateScreen(value.templateId))
            }
            is ChecklistCatalogEffect.NavigateToTemplateHistory -> {
                navigator.navigate(ChecklistHistoryScreenDestination(value.templateId))
            }
            null -> Unit
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            RecentChecklistsView(state.recentChecklistsLoadingState, viewModel::onEvent)
        }
        item {
            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.templates),
                style = MaterialTheme.typography.h5
            )
        }
        checklistTemplates(state, viewModel)
    }
}

private fun LazyListScope.checklistTemplates(
    state: ChecklistCatalogState,
    viewModel: ChecklistCatalogViewModel
) {
    when (val loadingState = state.templatesLoadingState) {
        ChecklistCatalogLoadingState.Loading -> {
            item {
                LoadingView()
            }
        }
        is ChecklistCatalogLoadingState.Success -> {
            if (loadingState.checklistTemplates.isEmpty()) {
                item {
                    NoChecklistTemplatesView()
                }
            } else {
                items(loadingState.checklistTemplates) {
                    ChecklistTemplateView(it, viewModel::onEvent)
                }
            }
        }
    }
}

@Composable
fun NoChecklistTemplatesView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text(
            modifier = Modifier
                .padding(all = 24.dp)
                .align(alignment = Alignment.Center),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.templates_empty)
        )
    }
}
