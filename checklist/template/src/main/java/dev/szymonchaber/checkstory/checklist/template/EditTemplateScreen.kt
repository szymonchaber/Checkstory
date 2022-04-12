package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import dev.szymonchaber.checkstory.checklist.template.model.*
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@Destination("edit_template_screen", start = true)
@Composable
fun EditTemplateScreen(
    navController: NavController,
    templateId: ChecklistTemplateId?
) {
    val viewModel = hiltViewModel<EditTemplateViewModel>()

    LaunchedEffect(templateId) {
        templateId?.let {
            viewModel.onEvent(EditTemplateEvent.EditChecklistTemplate(it))
        } ?: run {
            viewModel.onEvent(EditTemplateEvent.CreateChecklistTemplate)
        }
    }

    val state by viewModel.state.collectAsState(initial = EditTemplateState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(key1 = effect) {
        when (val value = effect) {
            is EditTemplateEffect.CloseScreen -> {
                navController.navigateUp()
            }
            null -> Unit
        }
    }
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
                    val checkboxes = loadingState.checklistTemplate.items.map(EditTemplateCheckbox::Existing)
                        .plus(loadingState.newCheckboxes.map(EditTemplateCheckbox::New))
                    EditTemplateView(loadingState.checklistTemplate, checkboxes, viewModel::onEvent)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(EditTemplateEvent.SaveTemplateClicked)
            }) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
            }
        }

    )
}

@Composable
fun EditTemplateView(
    checklistTemplate: ChecklistTemplate,
    checkboxes: List<EditTemplateCheckbox>,
    eventCollector: (EditTemplateEvent) -> Unit
) {
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
        checkboxes.forEach {
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
fun CheckboxItem(checkbox: EditTemplateCheckbox, eventCollector: (EditTemplateEvent) -> Unit) {
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
            checkbox.checkbox.title,
            { eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it)) },
            modifier = Modifier.align(CenterVertically)
        )
        IconButton(onClick = { eventCollector(EditTemplateEvent.ItemRemoved(checkbox)) }) {
            Icon(Icons.Filled.Close, "")
        }
    }
}
