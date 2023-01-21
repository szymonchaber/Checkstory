@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEvent
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEffect
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateState
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateViewModel
import dev.szymonchaber.checkstory.checklist.template.model.TemplateLoadingState
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.reminders.EditReminderViewModel
import dev.szymonchaber.checkstory.checklist.template.reminders.RemindersSection
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.EditReminderScreen
import dev.szymonchaber.checkstory.checklist.template.views.AddButton
import dev.szymonchaber.checkstory.checklist.template.views.CheckboxItem
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.ConfirmExitWithoutSavingDialog
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterialApi::class)
@Destination("edit_template_screen", start = true)
@Composable
fun EditTemplateScreen(
    navigator: DestinationsNavigator,
    templateId: ChecklistTemplateId?
) {
    trackScreenName("edit_template")
    val viewModel = hiltViewModel<EditTemplateViewModel>()
    val editReminderViewModel = hiltViewModel<EditReminderViewModel>()

    LaunchedEffect(templateId) {
        templateId?.let {
            viewModel.onEvent(EditTemplateEvent.EditChecklistTemplate(it))
        } ?: run {
            viewModel.onEvent(EditTemplateEvent.CreateChecklistTemplate)
        }
    }

    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    BackHandler {
        if (modalBottomSheetState.isVisible) {
            scope.launch {
                modalBottomSheetState.hide()
            }
        } else {
            viewModel.onEvent(EditTemplateEvent.BackClicked)
        }
    }

    val openConfirmExitDialog = remember { mutableStateOf(false) }
    if (openConfirmExitDialog.value) {
        ConfirmExitWithoutSavingDialog(openConfirmExitDialog) {
            viewModel.onEvent(EditTemplateEvent.ConfirmExitClicked)
            openConfirmExitDialog.value = false
        }
    }

    val openConfirmDeleteDialog = remember { mutableStateOf(false) }

    if (openConfirmDeleteDialog.value) {
        ConfirmDeleteTemplateDialog(openConfirmDeleteDialog) {
            viewModel.onEvent(EditTemplateEvent.ConfirmDeleteTemplateClicked)
            openConfirmDeleteDialog.value = false
        }
    }

    val state by viewModel.state.collectAsState(initial = EditTemplateState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is EditTemplateEffect.CloseScreen -> {
                navigator.navigateUp()
            }
            is EditTemplateEffect.ShowAddReminderSheet -> {
                editReminderViewModel.onEvent(EditReminderEvent.CreateReminder)
                scope.launch {
                    modalBottomSheetState.show()
                }
            }
            is EditTemplateEffect.ShowEditReminderSheet -> {
                editReminderViewModel.onEvent(EditReminderEvent.EditReminder(value.reminder))
                scope.launch {
                    modalBottomSheetState.show()
                }
            }
            is EditTemplateEffect.ShowConfirmDeleteDialog -> {
                openConfirmDeleteDialog.value = true
            }
            is EditTemplateEffect.ShowConfirmExitDialog -> {
                openConfirmExitDialog.value = true
            }
            is EditTemplateEffect.ShowFreeRemindersUsed -> {
                navigator.navigate(Routes.paymentScreen())
            }
            is EditTemplateEffect.OpenTemplateHistory -> {
                navigator.navigate(Routes.checklistHistoryScreen(value.templateId))
            }
            null -> Unit
        }
    }
    ModalBottomSheetLayout(
        sheetContent = {
            EditReminderScreen(viewModel = editReminderViewModel) {
                scope.launch {
                    modalBottomSheetState.hide()
                }
                viewModel.onEvent(EditTemplateEvent.ReminderSaved(it))
            }
        },
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        EditTemplateScaffold(templateId == null, state, viewModel)
    }
}

@Composable
private fun EditTemplateScaffold(
    isNewTemplate: Boolean,
    state: EditTemplateState,
    viewModel: EditTemplateViewModel
) {
    AdvertScaffold(
        topBar = {
            val titleText = if (isNewTemplate) {
                R.string.add_template
            } else {
                R.string.edit_template
            }
            TopAppBar(
                title = {
                    Text(text = stringResource(titleText))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(EditTemplateEvent.BackClicked)
                    }) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp,
                actions = {
                    val loadingState = state.templateLoadingState
                    if (loadingState is TemplateLoadingState.Success && loadingState.checklistTemplate.isStored) {
                        IconButton(onClick = {
                            viewModel.onEvent(EditTemplateEvent.TemplateHistoryClicked)
                        }) {
                            Icon(Icons.Filled.DateRange, "", tint = Color.White)
                        }
                    }
                }
            )
        },
        content = {
            when (val loadingState = state.templateLoadingState) {
                TemplateLoadingState.Loading -> {
                    FullSizeLoadingView()
                }
                is TemplateLoadingState.Success -> {
                    EditTemplateView(
                        loadingState.checklistTemplate,
                        loadingState.unwrappedCheckboxes,
                        viewModel::onEvent
                    )
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
    checkboxes: List<ViewTemplateCheckbox>,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        eventCollector(EditTemplateEvent.OnUnwrappedCheckboxMoved(from.viewKey!!, to.viewKey!!))
    }, canDragOver = { draggedOver, dragging ->
        draggedOver.isCheckbox
                && (dragging.viewKey!!.isParent || draggedOver.index > 1)
                && !(draggedOver.viewKey!!.isChild && dragging.viewKey!!.isParent)
    })
    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
    ) {
        item {
            ChecklistTemplateDetails(checklistTemplate, eventCollector)
        }
        items(
            items = checkboxes,
            key = { it.viewKey }
        ) {
            ReorderableItem(
                reorderableState = state,
                key = it.viewKey
            ) { isDragging ->
                SmartCheckboxItem(it, eventCollector, state, isDragging)
            }
        }
        item {
            AddTaskButton(eventCollector)
        }
        item {
            RemindersSection(checklistTemplate, eventCollector)
        }
        item {
            DeleteTemplateButton(eventCollector)
        }
    }
}

@Composable
private fun AddTaskButton(eventCollector: (EditTemplateEvent) -> Unit) {
    AddButton(
        modifier = Modifier.padding(start = 8.dp),
        onClick = { eventCollector(EditTemplateEvent.AddCheckboxClicked) },
        text = stringResource(R.string.new_checkbox)
    )
}

@Composable
private fun DeleteTemplateButton(eventCollector: (EditTemplateEvent) -> Unit) {
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

@Composable
private fun SmartCheckboxItem(
    checkbox: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit,
    state: ReorderableLazyListState,
    isDragging: Boolean
) {
    Row(
        Modifier.animateContentSize()
    ) {
        if (checkbox.isParent) {
            Column {
                ParentCheckbox(state, isDragging, checkbox, eventCollector)
                if (checkbox.children.isEmpty() && state.draggingItemKey == null) {
                    NewChildCheckboxButton(checkbox.viewKey, eventCollector)
                }
            }
        } else if (state.draggingItemKey != null && (state.draggingItemKey as? ViewTemplateCheckboxKey)?.isParent == true) {
            // do not render if any parent is moved
        } else {
            ChildCheckbox(state, isDragging, checkbox, eventCollector)
        }
    }
}

@Composable
private fun ParentCheckbox(
    state: ReorderableLazyListState,
    isDragging: Boolean,
    checkbox: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    CheckboxItem(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp),
        state = state,
        isDragging = isDragging,
        title = checkbox.title,
        checkbox is ViewTemplateCheckbox.New,
        onTitleChange = {
            eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
        },
    ) {
        eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
    }
}

@Composable
private fun ChildCheckbox(
    state: ReorderableLazyListState,
    isDragging: Boolean,
    checkbox: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Column {
        CheckboxItem(
            modifier = Modifier
                .padding(start = 44.dp, top = 8.dp, end = 16.dp),
            state = state,
            isDragging = isDragging,
            title = checkbox.title,
            checkbox is ViewTemplateCheckbox.New,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ChildItemTitleChanged(checkbox.parentViewKey!!, checkbox, it))
            },
        ) {
            eventCollector(
                EditTemplateEvent.ChildItemDeleted(
                    checkbox.parentViewKey!!,
                    checkbox
                )
            )
        }
        if (checkbox.isLastChild && state.draggingItemKey == null) {
            NewChildCheckboxButton(parent = checkbox.parentViewKey!!, eventCollector = eventCollector)
        }
    }
}

@Composable
fun NewChildCheckboxButton(
    parent: ViewTemplateCheckboxKey,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val text = stringResource(R.string.new_child_checkbox)
    AddButton(
        modifier = Modifier.padding(start = 36.dp, end = 16.dp),
        onClick = {
            eventCollector(EditTemplateEvent.ChildItemAdded(parent))
        },
        text = text
    )
}

@Composable
private fun ChecklistTemplateDetails(
    checklistTemplate: ChecklistTemplate,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.Sentences
        ),
        singleLine = true,
        maxLines = 1,
        value = checklistTemplate.title,
        label = { Text(text = stringResource(R.string.title)) },
        onValueChange = {
            eventCollector(EditTemplateEvent.TitleChanged(it))
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        )
    )
    OutlinedTextField(
        keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
        value = checklistTemplate.description,
        label = { Text(text = stringResource(R.string.description)) },
        onValueChange = {
            eventCollector(EditTemplateEvent.DescriptionChanged(it))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
    )
    SectionLabel(
        modifier = Modifier.padding(
            top = 16.dp,
            start = 16.dp
        ),
        text = stringResource(R.string.items)
    )
}

@Parcelize
data class ViewTemplateCheckboxKey(
    val viewId: Long,
    val parentKey: ViewTemplateCheckboxKey?,
    val isNew: Boolean,
    val isParent: Boolean
) : Parcelable {

    val isChild: Boolean
        get() = !isParent
}

val ViewTemplateCheckbox.viewKey: ViewTemplateCheckboxKey
    get() {
        return ViewTemplateCheckboxKey(
            id.id,
            parentViewKey,
            this is ViewTemplateCheckbox.New,
            isParent
        )
    }

val ItemPosition.viewKey: ViewTemplateCheckboxKey?
    get() {
        return key as? ViewTemplateCheckboxKey
    }

val ItemPosition.isCheckbox: Boolean
    get() {
        return key is ViewTemplateCheckboxKey
    }
