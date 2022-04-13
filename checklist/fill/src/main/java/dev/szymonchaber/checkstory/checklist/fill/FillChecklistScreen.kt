package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.fill.model.*
import dev.szymonchaber.checkstory.checklist.template.destinations.EditTemplateScreenDestination
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@Destination(route = "fill_checklist_screen", start = true)
@Composable
fun FillChecklistScreen(
    navigator: DestinationsNavigator,
    checklistId: ChecklistId?,
    createChecklistFrom: ChecklistTemplateId?
) {
    val viewModel = hiltViewModel<FillChecklistViewModel>()
    checklistId?.let {
        LaunchedEffect(it) {
            viewModel.onEvent(FillChecklistEvent.LoadChecklist(it))
        }
    }
    createChecklistFrom?.let {
        LaunchedEffect(it) {
            viewModel.onEvent(FillChecklistEvent.CreateChecklistFromTemplate(it))
        }
    }
    val state = viewModel.state.collectAsState(initial = FillChecklistState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is FillChecklistEffect.NavigateToEditTemplate -> {
                navigator.navigate(EditTemplateScreenDestination(value.templateId))
            }
            FillChecklistEffect.CloseScreen -> {
                navigator.navigateUp()
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Checklist")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigator.navigateUp()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp,
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(FillChecklistEvent.EditTemplateClicked)
                    }) {
                        Icon(Icons.Filled.Edit, "")
                    }
                }
            )
        }, content = {
            when (val loadingState = state.value.checklistLoadingState) {
                ChecklistLoadingState.Loading -> {
                    FillChecklistLoadingView()
                }
                is ChecklistLoadingState.Success -> {
                    FillChecklistView(loadingState.checklist, viewModel::onEvent)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(FillChecklistEvent.SaveChecklistClicked)
            }) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun FillChecklistLoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun FillChecklistView(checklist: Checklist, eventCollector: (FillChecklistEvent) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxHeight()
    ) {
        Text(
            modifier = Modifier.padding(start = 24.dp, top = 24.dp),
            text = checklist.title,
            style = MaterialTheme.typography.h4
        )
        Text(
            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp),
            text = checklist.description
        )
        Text(
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
            style = MaterialTheme.typography.caption,
            text = "Items",
        )
        checklist.items.forEach {
            CheckboxItem(it, eventCollector)
        }
        Text(
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
            style = MaterialTheme.typography.caption,
            text = "Notes",
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 96.dp),
            value = checklist.notes, onValueChange = {
                eventCollector(FillChecklistEvent.NotesChanged(it))
            }
        )
    }
}

@Composable
fun CheckboxItem(checkbox: Checkbox, eventCollector: (FillChecklistEvent) -> Unit) {
    Row(Modifier.padding(start = 16.dp, end = 16.dp)) {
        Checkbox(
            modifier = Modifier.align(CenterVertically),
            checked = checkbox.isChecked,
            onCheckedChange = {
                eventCollector(FillChecklistEvent.CheckChanged(checkbox, it))
            }
        )
        Text(modifier = Modifier.align(CenterVertically), text = checkbox.title)
    }
}
