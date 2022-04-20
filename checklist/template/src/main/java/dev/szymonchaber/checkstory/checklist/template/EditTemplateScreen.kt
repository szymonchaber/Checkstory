package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import dev.szymonchaber.checkstory.checklist.template.model.*
import dev.szymonchaber.checkstory.checklist.template.views.AddCheckboxButton
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

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
    LaunchedEffect(effect) {
        when (val value = effect) {
            is EditTemplateEffect.CloseScreen -> {
                navController.navigateUp()
            }
            null -> Unit
        }
    }
    AdvertScaffold(
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
                    FullSizeLoadingView()
                }
                is TemplateLoadingState.Success -> {
                    val items = loadingState.checklistTemplate.items.map {
                        it.copy(children = listOf(TemplateCheckbox(TemplateCheckboxId(0), "injected child", listOf())))
                    }
                    val checkboxes = items.map(EditTemplateCheckbox::Existing)
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTemplateView(
    checklistTemplate: ChecklistTemplate,
    checkboxes: List<EditTemplateCheckbox>,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            ChecklistTemplateDetails(checklistTemplate, eventCollector)
        }
        items(checkboxes, key = EditTemplateCheckbox::id) {
            ParentCheckboxItem(Modifier.animateItemPlacement(), it, eventCollector)
        }
        item {
            AddCheckboxButton(onClick = { eventCollector(EditTemplateEvent.AddCheckboxClicked) })
        }
        item {
            Box(Modifier.fillMaxWidth()) {
                DeleteButton(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    eventCollector(EditTemplateEvent.DeleteTemplateClicked)
                }
            }
        }
    }
}

@Composable
private fun ChecklistTemplateDetails(
    checklistTemplate: ChecklistTemplate,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    TextField(
        value = checklistTemplate.title,
        label = { Text(text = "Title") },
        onValueChange = {
            eventCollector(EditTemplateEvent.TitleChanged(it))
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.h4,
    )
    TextField(
        value = checklistTemplate.description,
        label = { Text(text = "Description") },
        onValueChange = {
            eventCollector(EditTemplateEvent.DescriptionChanged(it))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
    )
    Text(
        modifier = Modifier.padding(top = 16.dp),
        style = MaterialTheme.typography.caption,
        text = "Items",
    )
}
