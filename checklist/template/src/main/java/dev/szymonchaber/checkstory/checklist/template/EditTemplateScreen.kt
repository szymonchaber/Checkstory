package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateState
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateViewModel
import dev.szymonchaber.checkstory.checklist.template.model.TemplateLoadingState
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox

@Composable
fun EditTemplateScreen(
    viewModel: EditTemplateViewModel,
    navController: NavController,
    templateId: ChecklistTemplateId?
) {
    templateId?.let {
        LaunchedEffect(it) {
            viewModel.onEvent(EditTemplateEvent.EditChecklistTemplate(it))
        }
    } ?: run {
        LaunchedEffect(null) {
            viewModel.onEvent(EditTemplateEvent.CreateChecklistTemplate)
        }
    }
    val state by viewModel.state.collectAsState(initial = EditTemplateState.initial)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Edit template")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp
            )
        },
        content = {
            when (val loadingState = state.templateLoadingState) {
                TemplateLoadingState.Loading -> {
                    Text("Loading")
                }
                is TemplateLoadingState.Success -> {
                    EditTemplateView(loadingState.checklistTemplate, viewModel::onEvent)
                }
            }
        },
    )
}

@Composable
fun EditTemplateView(checklistTemplate: ChecklistTemplate, eventCollector: (EditTemplateEvent) -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxHeight()
            .padding(start = 24.dp, end = 24.dp)
    ) {
        TextField(
            value = checklistTemplate.title,
            onValueChange = {
                eventCollector(EditTemplateEvent.TitleChanged(it))
            },
            modifier = Modifier.padding(top = 24.dp),
            textStyle = MaterialTheme.typography.h4,
        )
        TextField(
            value = checklistTemplate.description,
            onValueChange = {
                eventCollector(EditTemplateEvent.DescriptionChanged(it))
            },
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.caption,
            text = "Items",
        )
        checklistTemplate.items.forEach {
            CheckboxItem(it, eventCollector)
        }
        AddCheckboxButton(eventCollector)
    }
}

@Composable
fun AddCheckboxButton(eventCollector: (EditTemplateEvent) -> Unit) {
    IconButton(onClick = { eventCollector(EditTemplateEvent.AddCheckboxClicked) }) {
        Icon(Icons.Filled.Add, null)
    }
}

@Composable
fun CheckboxItem(checkbox: TemplateCheckbox, eventCollector: (EditTemplateEvent) -> Unit) {
    Row(
        Modifier
            .padding(end = 16.dp)
            .fillMaxWidth()
    ) {
        Checkbox(
            modifier = Modifier.align(CenterVertically),
            checked = false,
            onCheckedChange = {
                // nop
            }
        )
        TextField(
            checkbox.title,
            { eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it)) },
            modifier = Modifier.align(CenterVertically)
        )
        IconButton(onClick = { eventCollector(EditTemplateEvent.ItemRemoved(checkbox)) }) {
            Icon(Icons.Filled.Close, "")
        }
    }
}
