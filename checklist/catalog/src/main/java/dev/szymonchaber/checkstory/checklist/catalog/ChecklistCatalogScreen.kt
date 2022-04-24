package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import dev.szymonchaber.checkstory.checklist.template.destinations.EditTemplateScreenDestination
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.LoadingView

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
                navigator.navigate(EditTemplateScreenDestination())
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
                navigator.navigate(EditTemplateScreenDestination(value.templateId))
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
            RecentChecklistsView(state.recentChecklistsLoadingState) { viewModel.onEvent(it) }
        }
        item {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.templates),
                style = MaterialTheme.typography.h5
            )
        }
        when (val loadingState = state.templatesLoadingState) {
            ChecklistCatalogLoadingState.Loading -> {
                item {
                    LoadingView()
                }
            }
            is ChecklistCatalogLoadingState.Success -> {
                items(loadingState.checklistTemplates) {
                    ChecklistTemplateView(it, viewModel::onEvent)
                }
            }
        }
    }
}
