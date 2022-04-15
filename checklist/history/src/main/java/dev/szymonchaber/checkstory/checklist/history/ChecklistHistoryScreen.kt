package dev.szymonchaber.checkstory.checklist.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import dev.szymonchaber.checkstory.checklist.fill.destinations.FillChecklistScreenDestination
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.CheckedItemsRatio
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@Composable
@Destination(route = "checklist_history", start = true)
fun ChecklistHistoryScreen(
    navigator: DestinationsNavigator,
    templateId: ChecklistTemplateId?
) {
    val viewModel = hiltViewModel<ChecklistHistoryViewModel>()
    LaunchedEffect(templateId) {
        viewModel.onEvent(ChecklistHistoryEvent.LoadChecklistHistory(templateId!!))
    }
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.checklist_history))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigator.navigateUp()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                elevation = 12.dp
            )
        },
        content = {
            ChecklistHistoryView(viewModel, navigator)
        },
    )
}

@Composable
private fun ChecklistHistoryView(
    viewModel: ChecklistHistoryViewModel,
    navigator: DestinationsNavigator
) {
    val state by viewModel.state.collectAsState(initial = ChecklistHistoryState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is ChecklistHistoryEffect.NavigateToFillChecklistScreen -> navigator.navigate(
                FillChecklistScreenDestination(
                    value.checklistId
                )
            )
            null -> Unit
        }
    }
    RecentChecklistsView(state = state, eventListener = viewModel::onEvent)
}

@Composable
fun RecentChecklistsView(state: ChecklistHistoryState, eventListener: (ChecklistHistoryEvent) -> Unit) {
    when (val loadingState = state.historyLoadingState) {
        HistoryLoadingState.Loading -> {
            FullSizeLoadingView()
        }
        is HistoryLoadingState.Success -> {
            ChecklistHistoryList(loadingState.checklists, eventListener)
        }
    }
}

@Composable
private fun ChecklistHistoryList(
    checklists: List<Checklist>,
    eventListener: (ChecklistHistoryEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(checklists) {
            ChecklistHistoryItem(it, eventListener)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChecklistHistoryItem(
    checklist: Checklist,
    eventListener: (ChecklistHistoryEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = 4.dp,
        onClick = {
            eventListener(ChecklistHistoryEvent.ChecklistClicked(checklist.id))
        }
    ) {
        val notes = checklist.notes.takeUnless(String::isBlank)?.let { "\"$it\"" } ?: "🙊"
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CheckedItemsRatio(checklist)
                DateFormatText(checklist.createdAt)
            }
        }
    }
}
