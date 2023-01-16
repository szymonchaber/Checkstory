@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import dev.szymonchaber.checkstory.checklist.template.views.ParentCheckboxItem
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.ConfirmExitWithoutSavingDialog
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
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
                    EditTemplateView(loadingState.checklistTemplate, viewModel::onEvent)
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
    eventCollector: (EditTemplateEvent) -> Unit
) {
    var checkboxes by remember {
        mutableStateOf(
            checkboxes().flatMap {
                listOf(it) + it.children
            }
        )
    }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        checkboxes = withUpdatedPosition(checkboxes, to, from)
    }, canDragOver = { draggedOver, dragging ->
        checkboxes.any { it == draggedOver.key }
                && ((dragging.checkbox)?.isParent == true || draggedOver.index > 1)
                && !(draggedOver.checkbox?.isChild == true && dragging.checkbox?.isParent == true)
//        )
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
            key = { it }
        ) {
            ReorderableItem(state, key = it) { isDragging ->
                WhatAmICheckboxItem(it, eventCollector, state, isDragging, checkboxes)
            }
        }
        item {
            AddButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = { eventCollector(EditTemplateEvent.AddCheckboxClicked) },
                text = stringResource(R.string.new_checkbox)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun LazyItemScope.WhatAmICheckboxItem(
    it: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit,
    state: ReorderableLazyListState,
    isDragging: Boolean,
    checkboxes: List<ViewTemplateCheckbox>
) {
    if (it.parentId == null) {
        ParentCheckboxItem(
            Modifier.Companion
                .animateItemPlacement()
                .padding(start = 16.dp, end = 16.dp),
            it,
            eventCollector,
            state,
            isDragging,
            state.draggingItemKey != null
        )
    } else if (state.draggingItemKey != null && (state.draggingItemKey as? ViewTemplateCheckbox)?.parentId == null) {
        // do not render if any parent is moved
    } else {
        val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
        Row(
            modifier = Modifier
                .padding(start = 48.dp, top = 8.dp, end = 0.dp)
                .animateContentSize()
                .shadow(elevation.value)
                .background(MaterialTheme.colors.surface)
        ) {
            Icon(
                modifier = Modifier
                    .detectReorder(state)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.drag_indicator),
                contentDescription = null
            )
            CheckboxItem(
                modifier = Modifier,
                title = it.title,
                it is ViewTemplateCheckbox.New,
                onTitleChange = {
//                eventCollector(EditTemplateEvent.ChildItemTitleChanged(checkbox, it, it))
                }
            ) {
//            eventCollector(EditTemplateEvent.ChildItemDeleted(checkbox, it))
            }
        }
    }
}

private fun checkboxes() = listOf(
    ViewTemplateCheckbox.New(
        TemplateCheckboxId(0),
        null,
        "Item 1",
        listOf(
            ViewTemplateCheckbox.New(
                TemplateCheckboxId(1),
                TemplateCheckboxId(0),
                "Child 1-1",
                listOf()
            ),
            ViewTemplateCheckbox.New(
                TemplateCheckboxId(2),
                TemplateCheckboxId(0),
                "Child 1-2",
                listOf()
            ),
            ViewTemplateCheckbox.New(
                TemplateCheckboxId(3),
                TemplateCheckboxId(0),
                "Child 1-3",
                listOf()
            )
        )
    ),
    ViewTemplateCheckbox.New(
        TemplateCheckboxId(4),
        null,
        "Item 2",
        listOf(
            ViewTemplateCheckbox.New(
                TemplateCheckboxId(5),
                TemplateCheckboxId(4),
                "Child 2-1",
                listOf()
            ),
            ViewTemplateCheckbox.New(
                TemplateCheckboxId(6),
                TemplateCheckboxId(4),
                "Child 2-2",
                listOf()
            ),
            ViewTemplateCheckbox.New(
                TemplateCheckboxId(7),
                TemplateCheckboxId(4),
                "Child 2-3",
                listOf()
            )
        )
    )
)

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun InlineBack(
    eventCollector: (EditTemplateEvent) -> Unit,
    checkboxes: List<ViewTemplateCheckbox>,
    checklistTemplate: ChecklistTemplate
) {
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        eventCollector(
            EditTemplateEvent.ParentItemsSwapped(
                from = from.key as ViewTemplateCheckbox,
                to = to.key as ViewTemplateCheckbox
            )
        )
    }, canDragOver = { a, b ->
        checkboxes.any { it == a.key }
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
            key = { it }
        ) {
            ReorderableItem(state, key = it) { isDragging ->
                ParentCheckboxItem(
                    Modifier
                        .animateItemPlacement()
                        .padding(start = 16.dp, end = 16.dp),
                    it,
                    eventCollector,
                    state,
                    isDragging,
                    state.draggingItemKey != null
                )
            }
        }
        item {
            AddButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = { eventCollector(EditTemplateEvent.AddCheckboxClicked) },
                text = stringResource(R.string.new_checkbox)
            )
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

val ItemPosition.checkbox: ViewTemplateCheckbox?
    get() {
        return key as? ViewTemplateCheckbox
    }
