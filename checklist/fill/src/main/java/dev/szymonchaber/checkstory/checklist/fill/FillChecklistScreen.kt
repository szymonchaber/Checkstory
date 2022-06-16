package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.fill.model.*
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.navigation.Routes

@Destination(
    route = "fill_checklist_screen",
    start = true,
    deepLinks = [
        DeepLink(
            uriPattern = "app://checkstory/checklist/new/{createChecklistFrom}"
        ),
        DeepLink(
            uriPattern = "app://checkstory/checklist/fill/{checklistId}"
        )
    ]
)
@Composable
fun FillChecklistScreen(
    navigator: DestinationsNavigator,
    checklistId: ChecklistId?,
    createChecklistFrom: ChecklistTemplateId?
) {
    trackScreenName("fill_checklist")
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
                navigator.navigate(Routes.editChecklistTemplateScreen(value.templateId))
            }
            FillChecklistEffect.CloseScreen -> {
                navigator.navigateUp()
            }
            null -> Unit
        }
    }

    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.checklist))
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
                    FullSizeLoadingView()
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

@Composable
fun FillChecklistView(checklist: Checklist, eventCollector: (FillChecklistEvent) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxHeight()
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            text = checklist.title,
            style = MaterialTheme.typography.h4
        )
        if (checklist.description.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp),
                text = checklist.description
            )
        }
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            style = MaterialTheme.typography.caption,
            text = stringResource(R.string.items),
        )
        checklist.items.forEach {
            CheckboxSection(checkbox = it, eventCollector = eventCollector)
        }
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            style = MaterialTheme.typography.caption,
            text = stringResource(R.string.notes),
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
            value = checklist.notes, onValueChange = {
                eventCollector(FillChecklistEvent.NotesChanged(it))
            }
        )
        DeleteButton(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(top = 24.dp, bottom = 144.dp)
        ) {
            eventCollector(FillChecklistEvent.DeleteChecklistClicked)
        }
    }
}

@Composable
fun CheckboxSection(checkbox: Checkbox, eventCollector: (FillChecklistEvent) -> Unit) {
    CheckboxItem(checkbox = checkbox) {
        eventCollector(FillChecklistEvent.CheckChanged(checkbox, it))
    }
    checkbox.children.forEach { child ->
        CheckboxItem(modifier = Modifier.padding(start = 24.dp), checkbox = child) {
            eventCollector(FillChecklistEvent.ChildCheckChanged(checkbox, child, it))
        }
    }
}
