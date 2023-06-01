@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import dev.szymonchaber.checkstory.checklist.template.model.OnboardingPlaceholders
import dev.szymonchaber.checkstory.checklist.template.model.TemplateLoadingState
import dev.szymonchaber.checkstory.checklist.template.reminders.EditReminderViewModel
import dev.szymonchaber.checkstory.checklist.template.reminders.RemindersSection
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.EditReminderScreen
import dev.szymonchaber.checkstory.checklist.template.reoder.DropTarget
import dev.szymonchaber.checkstory.checklist.template.reoder.DropTargetIndicatorLine
import dev.szymonchaber.checkstory.checklist.template.reoder.FloatingDraggable
import dev.szymonchaber.checkstory.checklist.template.reoder.LocalDragDropState
import dev.szymonchaber.checkstory.checklist.template.reoder.detectLazyListReorder
import dev.szymonchaber.checkstory.checklist.template.reoder.rememberDragDropState
import dev.szymonchaber.checkstory.checklist.template.views.AddButton
import dev.szymonchaber.checkstory.checklist.template.views.pleasantCharacterRemovalAnimationDurationMillis
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.ConfirmExitWithoutSavingDialog
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch
import java.time.Duration.*
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Destination("edit_template_screen", start = true)
@Composable
fun EditTemplateScreen(
    navigator: DestinationsNavigator,
    generateOnboarding: Boolean = false,
    templateId: TemplateId?,
) {
    trackScreenName("edit_template")
    val viewModel = hiltViewModel<EditTemplateViewModel>()
    val editReminderViewModel = hiltViewModel<EditReminderViewModel>()

    LaunchedEffect(templateId) {
        templateId?.let {
            viewModel.onEvent(EditTemplateEvent.EditTemplate(it))
        } ?: run {
            if (generateOnboarding) {
                viewModel.onEvent(EditTemplateEvent.GenerateOnboardingTemplate)
            } else {
                viewModel.onEvent(EditTemplateEvent.CreateTemplate)
            }
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

    val scaffoldState: ScaffoldState = rememberScaffoldState()

    val state by viewModel.state.collectAsState(initial = EditTemplateState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is EditTemplateEffect.CloseScreen -> {
                navigator.navigateUp()
            }
            is EditTemplateEffect.ShowAddReminderSheet -> {
                editReminderViewModel.onEvent(EditReminderEvent.CreateReminder(value.templateId))
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
            is EditTemplateEffect.ShowTryDraggingSnackbar -> {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = "Drag me to where you want a new task 🎯"
                    )
                }
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
        EditTemplateScaffold(scaffoldState, generateOnboarding, templateId == null, state, viewModel)
    }
}

val LocalRecentlyAddedUnconsumedItem = compositionLocalOf {
    RecentlyAddedUnconsumedItem()
}

val LocalIsReorderValidLookup = compositionLocalOf<(TemplateTaskId, TemplateTaskId) -> Boolean> {
    { _, _ ->
        true
    }
}

class RecentlyAddedUnconsumedItem {

    var item by mutableStateOf<TemplateTaskId?>(null)
}

@Composable
private fun EditTemplateScaffold(
    scaffoldState: ScaffoldState,
    isOnboarding: Boolean,
    isNewTemplate: Boolean,
    state: EditTemplateState,
    viewModel: EditTemplateViewModel
) {
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it, Modifier.padding(bottom = 48.dp))
        },
        topBar = {
            val titleText = when {
                isOnboarding -> {
                    R.string.create_your_first_checklist
                }
                isNewTemplate -> {
                    R.string.add_template
                }
                else -> {
                    R.string.edit_template
                }
            }
            TopAppBar(
                title = {
                    Text(text = stringResource(titleText))
                },
                navigationIcon = if (!isOnboarding) {
                    {
                        BackIcon {
                            viewModel.onEvent(EditTemplateEvent.BackClicked)
                        }
                    }
                } else {
                    null
                },
                elevation = 12.dp,
                actions = {
                    if (!isOnboarding && !isNewTemplate) {
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
            Box(
                Modifier.padding(it)
            ) {
                when (val loadingState = state.templateLoadingState) {
                    TemplateLoadingState.Loading -> {
                        FullSizeLoadingView()
                    }
                    is TemplateLoadingState.Success -> {
                        val recentlyAddedUnconsumedItem = remember {
                            RecentlyAddedUnconsumedItem()
                        }
                        LaunchedEffect(key1 = loadingState.mostRecentlyAddedItem) {
                            recentlyAddedUnconsumedItem.item = loadingState.mostRecentlyAddedItem
                        }
                        CompositionLocalProvider(
                            LocalRecentlyAddedUnconsumedItem provides recentlyAddedUnconsumedItem,
                            LocalDragDropState provides rememberDragDropState(),
                            LocalIsReorderValidLookup provides { subject, target ->
                                viewModel.isReorderValid(subject, target)
                            }
                        ) {
                            EditTemplateView(loadingState, viewModel::onEvent)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun BackIcon(onBackClicked: () -> Unit) {
    IconButton(onClick = onBackClicked) {
        Icon(Icons.Filled.ArrowBack, "")
    }
}

val nestedPaddingStart = 32.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTemplateView(
    success: TemplateLoadingState.Success,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val dragDropState = LocalDragDropState.current
    val recentlyAddedItem = LocalRecentlyAddedUnconsumedItem.current
    LaunchedEffect(recentlyAddedItem.item) {
        recentlyAddedItem.item?.let { newItem ->
            val isNewItemNotVisible =
                dragDropState.lazyListState.layoutInfo.visibleItemsInfo.none { it.key as? TemplateTaskId == newItem }
            if (isNewItemNotVisible) {
                dragDropState.lazyListState.animateScrollToItem(success.unwrappedTasks.indexOfFirst { it.first.id == newItem } + 1)
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val template = success.template
                LazyColumn(
                    Modifier
                        .detectLazyListReorder()
                        .fillMaxWidth(),
                    state = dragDropState.lazyListState,
                ) {
                    item {
                        TemplateDetails(template, success.onboardingPlaceholders, eventCollector)
                    }
                    items(
                        items = success.unwrappedTasks,
                        key = { (item, _) ->
                            item.id
                        }
                    ) { (task, nestingLevel) ->
                        Row(
                            Modifier.animateItemPlacement()
                        ) {
                            val startPadding by animateDpAsState(
                                nestedPaddingStart * nestingLevel
                            )
                            CommonTask(
                                task = task,
                                paddingStart = startPadding,
                                nestingLevel = nestingLevel,
                                eventCollector = eventCollector
                            )
                        }
                    }
                    item {
                        Box(Modifier.height(IntrinsicSize.Min)) {
                            Column {
                                AddTaskButton(eventCollector)
                                RemindersSection(template, eventCollector)
                                DeleteTemplateButton(eventCollector)
                            }
                            DropTarget(
                                modifier = Modifier
                                    .fillMaxSize(),
//                            .background(Color.Green.copy(alpha = 0.2f)),
                                placeTargetLineOnTop = true,
                                onDataDropped = { taskKey ->
                                    if (taskKey.id == NEW_TASK_ID) {
                                        eventCollector(EditTemplateEvent.NewTaskDraggedToBottom)
                                    } else {
                                        eventCollector(EditTemplateEvent.TaskMovedToBottom(taskKey))
                                    }
                                },
                                dropTargetOffset = 16.dp
                            )
                        }
                    }
                }
                DropTargetIndicatorLine()
            }
            BottomActionBar(eventCollector = eventCollector)
        }
        FloatingDraggable(success)
        dragDropState.scrollComparisonDebugPoints?.let { (top, bottom) ->
//            DebugFloatingPoint(top, Color.Blue)
//            DebugFloatingPoint(bottom, Color.Red)
        }
        dragDropState.pointerDebugPoint?.let {
//            DebugFloatingPoint(offset = it, color = Color.Green)
        }
    }
}

@Composable
fun BottomActionBar(eventCollector: (EditTemplateEvent) -> Unit) {
    Card(
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Draggable(
                modifier = Modifier
                    .weight(0.5f)
                    .align(Alignment.CenterVertically)
            ) {
                NewTask(it.fillMaxWidth()) {
                    eventCollector(EditTemplateEvent.NewTaskDraggableClicked)
                }
            }
            Button(
                modifier = Modifier
                    .weight(0.5f)
                    .align(Alignment.CenterVertically),
                shape = MaterialTheme.shapes.medium,
                onClick = {
                    eventCollector(EditTemplateEvent.SaveTemplateClicked)
                }) {
                Text(text = stringResource(R.string.save_template))
            }
        }
    }

}


@Composable
private fun DebugFloatingPoint(offset: Offset, color: Color) {
    Box(modifier = Modifier
        .graphicsLayer {
            translationX = offset.x
            translationY = offset.y
        }
        .background(color)
        .size(4.dp)
        .clip(CircleShape))
}

@Composable
private fun AddTaskButton(eventCollector: (EditTemplateEvent) -> Unit) {
    AddButton(
        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
        onClick = { eventCollector(EditTemplateEvent.AddTaskClicked) },
        text = stringResource(R.string.new_task)
    )
}

@Composable
private fun DeleteTemplateButton(eventCollector: (EditTemplateEvent) -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        DeleteButton(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 96.dp)
                .align(Alignment.TopCenter)
        ) {
            eventCollector(EditTemplateEvent.DeleteTemplateClicked)
        }
    }
}

@Composable
private fun TemplateDetails(
    template: Template,
    onboardingPlaceholders: OnboardingPlaceholders?,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Box(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.padding(top = 16.dp)) {
            TitleTextField(template, onboardingPlaceholders, eventCollector)
            DescriptionTextField(template, onboardingPlaceholders, eventCollector)
            SectionLabel(
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 16.dp
                ),
                text = stringResource(R.string.items)
            )
        }
        DropTarget(
            modifier = Modifier
                .fillMaxSize(),
//                .background(Color.Blue.copy(alpha = 0.2f)),
            onDataDropped = { taskKey ->
                if (taskKey.id == NEW_TASK_ID) {
                    eventCollector(EditTemplateEvent.NewTaskDraggedToTop)
                } else {
                    eventCollector(EditTemplateEvent.TaskMovedToTop(taskKey))
                }
            },
            dropTargetOffset = 16.dp
        )
    }
}

@Composable
private fun TitleTextField(
    template: Template,
    onboardingPlaceholders: OnboardingPlaceholders?,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    TextFieldWithFixedPlaceholder(
        value = template.title,
        onValueChange = {
            eventCollector(EditTemplateEvent.TitleChanged(it))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        singleLine = true,
        label = stringResource(R.string.title),
        placeholder = onboardingPlaceholders?.title,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.Sentences
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        )
    )

}

@Composable
private fun DescriptionTextField(
    template: Template,
    onboardingPlaceholders: OnboardingPlaceholders?,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    TextFieldWithFixedPlaceholder(
        value = template.description,
        onValueChange = {
            eventCollector(EditTemplateEvent.DescriptionChanged(it))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        label = stringResource(R.string.description),
        placeholder = onboardingPlaceholders?.description,
        keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
    )
}

@Composable
private fun TextFieldWithFixedPlaceholder(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String?,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var showActualValue by remember {
        mutableStateOf(false)
    }
    var placeholderCharactersDisplayed by remember(placeholder) {
        mutableStateOf(placeholder?.count() ?: 0)
    }
    val animatedCharacterCount by animateIntAsState(
        targetValue = placeholderCharactersDisplayed,
        animationSpec = tween(
            durationMillis = pleasantCharacterRemovalAnimationDurationMillis * (placeholder?.length ?: 1)
        )
    ) {
        if (it == 0) {
            showActualValue = true
        }
    }
    val textValue = if (showActualValue || value.isNotEmpty()) {
        value
    } else {
        placeholder?.take(animatedCharacterCount) ?: ""
    }
    OutlinedTextField(
        keyboardOptions = keyboardOptions,
        value = textValue,
        singleLine = singleLine,
        maxLines = maxLines,
        label = { Text(text = label) },
        placeholder = placeholder?.let {
            { Text(text = it) }
        },
        keyboardActions = keyboardActions,
        onValueChange = onValueChange,
        modifier = modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    placeholderCharactersDisplayed = 0
                }
            },
    )
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

fun Modifier.then(modifier: Modifier.() -> Modifier): Modifier {
    return then(modifier(Modifier))
}
