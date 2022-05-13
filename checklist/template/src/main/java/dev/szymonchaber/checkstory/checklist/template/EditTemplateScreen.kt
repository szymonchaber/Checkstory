@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEffect
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateState
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateViewModel
import dev.szymonchaber.checkstory.checklist.template.model.TemplateLoadingState
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.reminders.RemindersSection
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.EditReminderScreen
import dev.szymonchaber.checkstory.checklist.template.views.AddCheckboxButton
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState(initial = EditTemplateState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is EditTemplateEffect.CloseScreen -> {
                navController.navigateUp()
            }
            EditTemplateEffect.ShowAddReminderSheet -> {
                scope.launch {
                    modalBottomSheetState.show()
                }
            }
            null -> Unit
        }
    }
    ModalBottomSheetLayout(
        sheetContent = {
            EditReminderScreen()
        },
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor = Color.White,
        // scrimColor = Color.Red,  // Color for the fade background when you open/close the drawer
    ) {
        EditTemplateScaffold(navController, state, viewModel, scope, modalBottomSheetState)
    }
}

@Composable
private fun EditTemplateScaffold(
    navController: NavController,
    state: EditTemplateState,
    viewModel: EditTemplateViewModel,
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState
) {
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.edit_template))
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
                    EditTemplateView(loadingState.checklistTemplate, loadingState.checkboxes, viewModel::onEvent)
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
    checkboxes: List<ViewTemplateCheckbox>,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ChecklistTemplateDetails(checklistTemplate, eventCollector)
        }
        items(
            checkboxes,
//            key = { it.id.id } // TODO causes crashes & animations don't work anyways - fix at some point
        ) {
            ParentCheckboxItem(Modifier.animateItemPlacement(), it, eventCollector)
        }
        item {
            AddCheckboxButton(onClick = { eventCollector(EditTemplateEvent.AddCheckboxClicked) })
        }
        item {
            RemindersSection(checklistTemplate, eventCollector)
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
    OutlinedTextField(
        value = checklistTemplate.title,
        label = { Text(text = stringResource(R.string.title)) },
        onValueChange = {
            eventCollector(EditTemplateEvent.TitleChanged(it))
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.h4,
    )
    OutlinedTextField(
        value = checklistTemplate.description,
        label = { Text(text = stringResource(R.string.description)) },
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
        text = stringResource(R.string.items),
    )
}
