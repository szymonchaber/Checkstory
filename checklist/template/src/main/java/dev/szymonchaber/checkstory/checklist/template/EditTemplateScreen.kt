@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import dev.szymonchaber.checkstory.checklist.template.model.*
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

@OptIn(ExperimentalMaterialApi::class)
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
            EditReminderScreen {
                scope.launch {
                    modalBottomSheetState.hide()
                }
                viewModel.onEvent(EditTemplateEvent.ReminderSaved(it))
            }
        },
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        singleLine = true,
        maxLines = 1,
        value = checklistTemplate.title,
        label = { Text(text = stringResource(R.string.title)) },
        onValueChange = {
            eventCollector(EditTemplateEvent.TitleChanged(it))
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        )
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
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
