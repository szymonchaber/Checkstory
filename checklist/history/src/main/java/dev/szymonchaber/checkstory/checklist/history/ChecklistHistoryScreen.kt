package dev.szymonchaber.checkstory.checklist.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Checklist history")
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
            is ChecklistHistoryEffect.ExampleEffect -> Unit
            null -> Unit
        }
    }
    RecentChecklistsView(state = state, viewModel = viewModel)
}

@Composable
fun RecentChecklistsView(state: ChecklistHistoryState, viewModel: ChecklistHistoryViewModel) {
    when (val loadingState = state.historyLoadingState) {
        HistoryLoadingState.Loading -> {
            Text(text = "Loading")
        }
        is HistoryLoadingState.Success -> {
            ChecklistHistoryList(viewModel, loadingState.checklists)
        }
    }
}

@Composable
private fun ChecklistHistoryList(
    viewModel: ChecklistHistoryViewModel,
    checklists: List<Checklist>
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(checklists) {
            ChecklistHistoryItem(it, viewModel::onEvent)
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


