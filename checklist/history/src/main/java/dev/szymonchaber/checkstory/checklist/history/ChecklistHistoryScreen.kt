package dev.szymonchaber.checkstory.checklist.history

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.CheckedItemsRatio
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.navigation.Routes

@SuppressLint("MissingPermission")
@Composable
@Destination(route = "checklist_history", start = true)
fun ChecklistHistoryScreen(
    navigator: DestinationsNavigator,
    templateId: TemplateId?
) {
    trackScreenName("checklist_history")
    val viewModel = hiltViewModel<ChecklistHistoryViewModel>()
    LaunchedEffect(templateId) {
        viewModel.onEvent(ChecklistHistoryEvent.LoadChecklistHistory(templateId!!))
    }
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.template_checklists))
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
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is ChecklistHistoryEffect.NavigateToFillChecklistScreen -> {
                    navigator.navigate(Routes.editChecklistScreen(it.checklistId))
                }
            }
        }
    }
    when (val loadingState = state.loadingState) {
        HistoryLoadingState.Loading -> {
            FullSizeLoadingView()
        }

        is HistoryLoadingState.Success -> {
            if (loadingState.checklists.isEmpty()) {
                NoChecklistsInHistoryView()
            } else {
                ChecklistHistoryList(loadingState.checklists, viewModel::onEvent)
            }
        }
    }
}

@Composable
fun NoChecklistsInHistoryView() {
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
            text = stringResource(R.string.checklist_history_empty)
        )
    }
}

@Composable
private fun ChecklistHistoryList(
    checklists: List<Checklist>,
    eventListener: (ChecklistHistoryEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(all = 16.dp),
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
        elevation = 2.dp,
        onClick = {
            eventListener(ChecklistHistoryEvent.ChecklistClicked(checklist.id))
        }
    ) {
        val notesFontStyle = if (checklist.notes.isBlank()) {
            FontStyle.Italic
        } else {
            FontStyle.Normal
        }
        val notesOrEmptyNotesText = checklist.notes.ifBlank {
            stringResource(id = R.string.no_notes)
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
                text = notesOrEmptyNotesText,
                style = MaterialTheme.typography.subtitle1.copy(fontStyle = notesFontStyle, fontSize = 14.sp)
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
