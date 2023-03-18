@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
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
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.reminders.EditReminderViewModel
import dev.szymonchaber.checkstory.checklist.template.reminders.RemindersSection
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.EditReminderScreen
import dev.szymonchaber.checkstory.checklist.template.views.AddButton
import dev.szymonchaber.checkstory.checklist.template.views.CheckboxItem
import dev.szymonchaber.checkstory.checklist.template.views.NewCheckboxItem
import dev.szymonchaber.checkstory.checklist.template.views.pleasantCharacterRemovalAnimationDurationMillis
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.ConfirmExitWithoutSavingDialog
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.Duration.*

@OptIn(ExperimentalMaterialApi::class)
@Destination("edit_template_screen", start = true)
@Composable
fun EditTemplateScreen(
    navigator: DestinationsNavigator,
    generateOnboarding: Boolean = false,
    templateId: ChecklistTemplateId?,
) {
    trackScreenName("edit_template")
    val viewModel = hiltViewModel<EditTemplateViewModel>()
    val editReminderViewModel = hiltViewModel<EditReminderViewModel>()

    LaunchedEffect(templateId) {
        templateId?.let {
            viewModel.onEvent(EditTemplateEvent.EditChecklistTemplate(it))
        } ?: run {
            if (generateOnboarding) {
                viewModel.onEvent(EditTemplateEvent.GenerateOnboardingChecklistTemplate)
            } else {
                viewModel.onEvent(EditTemplateEvent.CreateChecklistTemplate)
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
        EditTemplateScaffold(generateOnboarding, templateId == null, state, viewModel)
    }
}

val RecentlyAddedUnconsumedItem = compositionLocalOf<ViewTemplateCheckboxKey?> {
    null
}

@Composable
private fun EditTemplateScaffold(
    isOnboarding: Boolean,
    isNewTemplate: Boolean,
    state: EditTemplateState,
    viewModel: EditTemplateViewModel
) {
    AdvertScaffold(
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
            when (val loadingState = state.templateLoadingState) {
                TemplateLoadingState.Loading -> {
                    FullSizeLoadingView()
                }
                is TemplateLoadingState.Success -> {
                    var recentlyAddedUnconsumedItem by remember {
                        mutableStateOf<ViewTemplateCheckboxKey?>(null)
                    }
                    LaunchedEffect(key1 = loadingState.mostRecentlyAddedItem) {
                        recentlyAddedUnconsumedItem = loadingState.mostRecentlyAddedItem
                    }
                    CompositionLocalProvider(RecentlyAddedUnconsumedItem provides recentlyAddedUnconsumedItem) {
//                        EditTemplateView(
                        NewEditTemplateView(
                            loadingState,
                            viewModel::onEvent
                        ) {
                            recentlyAddedUnconsumedItem = null
                        }
                    }
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
private fun BackIcon(onBackClicked: () -> Unit) {
    IconButton(onClick = onBackClicked) {
        Icon(Icons.Filled.ArrowBack, "")
    }
}

val nestedPaddingStart = 32.dp

@Composable
fun EditTemplateView(
    success: TemplateLoadingState.Success,
    eventCollector: (EditTemplateEvent) -> Unit,
    onAddedItemConsumed: () -> Unit
) {
    val template = success.checklistTemplate
    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            ChecklistTemplateDetails(template, success.onboardingPlaceholders, eventCollector)
        }
        items(
            items = success.checkboxes,
            key = { it.viewKey }
        ) { checkbox ->
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                CommonCheckbox(
                    checkbox = checkbox,
                    paddingStart = nestedPaddingStart,
                    isLastChild = true,
                    onAddedItemConsumed = onAddedItemConsumed,
                    eventCollector = eventCollector
                )
            }
        }
        item {
            AddTaskButton(eventCollector)
        }
        item {
            RemindersSection(template, eventCollector)
        }
        item {
            DeleteTemplateButton(eventCollector)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewEditTemplateView(
    success: TemplateLoadingState.Success,
    eventCollector: (EditTemplateEvent) -> Unit,
    onAddedItemConsumed: () -> Unit
) {
    val dragDropState = rememberDragDropState(currentTasksProvider = {
        success.unwrappedCheckboxes
    })
    LaunchedEffect(dragDropState.isDragging) {
        val data = if (!dragDropState.isDragging) {
            dragDropState.dataToDrop
        } else {
            null
        }
        data?.let {
            dragDropState.dataToDrop = null
            dragDropState.currentDropTarget?.invoke(it)
        }
    }
    CompositionLocalProvider(
        LocalDragDropState provides dragDropState,
    ) {
        Box(Modifier.fillMaxSize()) {
            val scope = rememberCoroutineScope()
            var overscrollJob by remember { mutableStateOf<Job?>(null) }
            val template = success.checklistTemplate
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDrag = { change, offset ->
                                change.consume()
                                dragDropState.onDrag(offset)
                                if (overscrollJob?.isActive == true)
                                    return@detectDragGesturesAfterLongPress

                                dragDropState
                                    .checkForOverScroll()
                                    .takeIf { it != 0f }
                                    ?.let {
                                        overscrollJob =
                                            scope.launch { dragDropState.lazyListState.scrollBy(it) }
                                    }
                                    ?: run { overscrollJob?.cancel() }
                            },
                            onDragStart = { offset -> dragDropState.onDragStart(offset) },
                            onDragEnd = { dragDropState.onDragInterrupted() },
                            onDragCancel = { dragDropState.onDragInterrupted() }
                        )
                    },
                state = dragDropState.lazyListState,
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    ChecklistTemplateDetails(template, success.onboardingPlaceholders, eventCollector)
                }
                items(
                    items = success.unwrappedCheckboxes,
                    key = { (item, _) ->
                        item.viewKey
                    }
                ) { (checkbox, nestingLevel) ->
                    Row(
                        Modifier
                            .animateItemPlacement()
                            .padding(start = 16.dp, end = 16.dp) // TODO what this?
                    ) {
                        val startPadding by animateDpAsState(
                            nestedPaddingStart * nestingLevel
                        )
                        NewCommonCheckbox(
                            checkbox = checkbox,
                            paddingStart = startPadding,
                            nestingLevel = nestingLevel,
                            isLastChild = true,
                            onAddedItemConsumed = onAddedItemConsumed,
                            eventCollector = eventCollector
                        )
                    }
                }
                item {
                    AddTaskButton(eventCollector)
                }
                item {
                    RemindersSection(template, eventCollector)
                }
                item {
                    DeleteTemplateButton(eventCollector)
                }
            }
            DropTargetIndicatorLine()
            if (dragDropState.isDragging) {
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = (dragDropState.dragPosition + dragDropState.dragOffset)
                            alpha = if (targetSize == IntSize.Zero) 0f else .9f
                            translationX = offset.x//.minus(12.dp.toPx())
//                            translationY = offset.y//.minus(targetSize.height * 2 + 0.dp.toPx())
                            translationY = offset.y
//                                dragDropState.dragPosition.y + (dragDropListStateMine.elementDisplacement ?: 0f)
                        }
                        .onGloballyPositioned {
                            targetSize = it.size
                        }
                ) {
                    dragDropState.draggableComposable?.invoke(Modifier)
                }
            }
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
private fun CommonCheckbox(
    checkbox: ViewTemplateCheckbox,
    paddingStart: Dp,
    isLastChild: Boolean,
    nestingLevel: Int = 1,
    onAddedItemConsumed: () -> Unit,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    Column {
        val taskTopPadding = 8.dp
        val paddingStartActual = if (nestingLevel > 1) paddingStart else 0.dp
        val focusRequester = remember { FocusRequester() }
        CheckboxItem(
            modifier = Modifier
                .drawBehind { // TODO check drawWithContent or withCache
                    if (nestingLevel > 1) {
                        val heightFraction = if (!isLastChild) 1f else 0.5f
                        drawLine(
                            color = Color.Gray,
                            start = Offset.Zero,
                            end = Offset(0f, size.height * heightFraction + taskTopPadding.toPx() / 2),
                            strokeWidth = 4.dp.toPx()
                        )
                        val visualCenterY = center.y + taskTopPadding.toPx() / 2
                        drawLine(
                            color = Color.Gray,
                            start = Offset(x = 0f, y = visualCenterY),
                            end = Offset(x = paddingStart.toPx(), y = visualCenterY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                .padding(top = taskTopPadding, start = paddingStartActual),
            title = checkbox.title,
            placeholder = checkbox.placeholderTitle,
            nestingLevel = nestingLevel,
            focusRequester = focusRequester,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            },
            onAddSubtask = {
                eventCollector(EditTemplateEvent.ChildItemAdded(checkbox.viewKey))
            }
        ) {
            eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
        }
        val recentlyAddedItem = RecentlyAddedUnconsumedItem.current
        LaunchedEffect(recentlyAddedItem) {
            if (checkbox.viewKey == recentlyAddedItem) {
                focusRequester.requestFocus()
                onAddedItemConsumed()
            }
        }
        val paddingMultiplier = if (nestingLevel == 1) {
            1
        } else {
            2
        }
        Row {
            val localDensity = LocalDensity.current
            var columnHeightDp by remember {
                mutableStateOf(0.dp)
            }
            if (!isLastChild) {
                Box(
                    modifier = Modifier
                        .height(columnHeightDp)
                        .background(Color.Gray)
                        .width(2.dp)
                )
            }
            Column(
                Modifier
                    .padding(start = paddingStart * paddingMultiplier)
                    .animateContentSize()
                    .onGloballyPositioned {
                        columnHeightDp = with(localDensity) { it.size.height.toDp() }
                    }) {
                checkbox.children.forEachIndexed { index, child ->
                    CommonCheckbox(
                        checkbox = child,
                        paddingStart = paddingStart,
                        isLastChild = checkbox.children.lastIndex == index,
                        nestingLevel = nestingLevel + 1,
                        onAddedItemConsumed = onAddedItemConsumed,
                        eventCollector = eventCollector,
                    )
                }
            }
        }
    }
}

@Composable
private fun NewCommonCheckbox(
    checkbox: ViewTemplateCheckbox,
    paddingStart: Dp,
    isLastChild: Boolean,
    nestingLevel: Int,
    onAddedItemConsumed: () -> Unit,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    val taskTopPadding = 8.dp
    val focusRequester = remember { FocusRequester() }
    Box(Modifier.height(IntrinsicSize.Min)) {

        NewCheckboxItem(
            modifier = Modifier
//                .drawFolderStructure(nestingLevel, isLastChild, paddingStart, taskTopPadding) TODO decide if this should stay
                .padding(top = taskTopPadding, start = paddingStart),
            title = checkbox.title,
            placeholder = checkbox.placeholderTitle,
            nestingLevel = nestingLevel,
            focusRequester = focusRequester,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            },
            onAddSubtask = {
                eventCollector(EditTemplateEvent.ChildItemAdded(checkbox.viewKey))
            }
        ) {
            eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
        }
        Receptacles(
            modifier = Modifier.padding(top = taskTopPadding, start = paddingStart),
            onSiblingTaskDropped = { siblingTask ->
                eventCollector(EditTemplateEvent.SiblingMovedBelow(checkbox.viewKey, siblingTask))
            },
            onChildTaskDropped = { childTask ->
                eventCollector(EditTemplateEvent.ChildMovedBelow(checkbox.viewKey, childTask))
            }
        )
    }
    val recentlyAddedItem = RecentlyAddedUnconsumedItem.current
    LaunchedEffect(recentlyAddedItem) {
        if (checkbox.viewKey == recentlyAddedItem) {
            focusRequester.requestFocus()
            onAddedItemConsumed() // TODO consume in-place by editing the mutableStateOf
        }
    }
}

@Composable
private fun ChecklistTemplateDetails(
    checklistTemplate: ChecklistTemplate,
    onboardingPlaceholders: OnboardingPlaceholders?,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Box(Modifier.height(IntrinsicSize.Min)) {
        Column {
            TitleTextField(checklistTemplate, onboardingPlaceholders, eventCollector)
            DescriptionTextField(checklistTemplate, onboardingPlaceholders, eventCollector)
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
            onDataDropped = { taskKey ->
                eventCollector(EditTemplateEvent.CheckboxMovedToTop(taskKey))
            },
        )
    }
}

@Composable
private fun TitleTextField(
    checklistTemplate: ChecklistTemplate,
    onboardingPlaceholders: OnboardingPlaceholders?,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    TextFieldWithFixedPlaceholder(
        value = checklistTemplate.title,
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
    checklistTemplate: ChecklistTemplate,
    onboardingPlaceholders: OnboardingPlaceholders?,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    TextFieldWithFixedPlaceholder(
        value = checklistTemplate.description,
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

@Parcelize
data class ViewTemplateCheckboxKey(
    val viewId: Long,
    val parentKey: ViewTemplateCheckboxKey?,
    val isNew: Boolean
) : Parcelable

val ViewTemplateCheckbox.viewKey: ViewTemplateCheckboxKey
    get() {
        return ViewTemplateCheckboxKey(
            id.id,
            parentViewKey,
            this is ViewTemplateCheckbox.New
        )
    }

class DragDropState(
    val getCurrentTasks: () -> List<Pair<ViewTemplateCheckbox, Int>>,
    val lazyListState: LazyListState,
) {

    // region mine
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable (Modifier) -> Unit)?>(null)
    var dataToDrop by mutableStateOf<ViewTemplateCheckboxKey?>(null)

    var currentDropTarget: ((ViewTemplateCheckboxKey) -> Unit)? by mutableStateOf(null)
    var currentDropTargetPosition: Offset? by mutableStateOf(null)

    // endregion

    var draggedDistance by mutableStateOf(0f)

    var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.takeUnless { it.key !is ViewTemplateCheckboxKey }
            ?.also { itemInfo ->
                currentIndexOfDraggedItem = itemInfo.index
                initiallyDraggedElement = itemInfo
                dragPosition = Offset(0f, itemInfo.offset.toFloat())
                isDragging = true
                dataToDrop = itemInfo.key as ViewTemplateCheckboxKey
                draggableComposable = { _ ->
                    val (task, nestingLevel) = getCurrentTasks().firstOrNull { it.first.viewKey == itemInfo.key }!!
                    val focusRequester = remember { FocusRequester() }
                    NewCheckboxItem(
                        title = task.title,
                        placeholder = task.placeholderTitle,
                        isFunctional = false,
                        nestingLevel = nestingLevel,
                        focusRequester = focusRequester,
                        onTitleChange = {},
                        onAddSubtask = {},
                        onDeleteClick = {}
                    )
                }
            }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
        isDragging = false
        dragOffset = Offset.Zero
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y
        dragOffset += offset
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance

            when {
                draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this.layoutInfo.visibleItemsInfo.getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

val LocalDragDropState = compositionLocalOf<DragDropState> {
    error("You must provide LocalDragDropState")
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState = rememberLazyListState(),
    currentTasksProvider: () -> List<Pair<ViewTemplateCheckbox, Int>>,
): DragDropState {
    return remember {
        DragDropState(
            getCurrentTasks = currentTasksProvider,
            lazyListState = lazyListState,
        )
    }
}

@Composable
private fun Receptacles(
    onSiblingTaskDropped: (ViewTemplateCheckboxKey) -> Unit,
    onChildTaskDropped: (ViewTemplateCheckboxKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxSize()) {
        DropTarget(
            modifier = Modifier
                .fillMaxHeight()
//                .background(Color.Red.copy(alpha = 0.2f))
                .width(24.dp), // TODO decide
            onDataDropped = { siblingTask ->
                onSiblingTaskDropped(siblingTask)
            }
        )
        DropTarget(
            modifier = Modifier
                .fillMaxHeight()
//                .background(Color.Yellow.copy(alpha = 0.2f))
                .weight(1f),
            onDataDropped = { childTask ->
                onChildTaskDropped(childTask)
            }
        )
    }
}


@Composable
fun DropTarget(
    modifier: Modifier,
    onDataDropped: (ViewTemplateCheckboxKey) -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {},
    placeTargetLineOnTop: Boolean = false
) {
    val dragInfo = LocalDragDropState.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    val density = LocalDensity.current

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            val isCurrentDropTarget =
                rect.contains(dragPosition + dragOffset + Offset(0f, density.run { 96.dp.toPx() }))
            if (isCurrentDropTarget) {
                dragInfo.currentDropTarget = onDataDropped
                val yOffset = if (placeTargetLineOnTop) 0f else it.size.height.toFloat()
                dragInfo.currentDropTargetPosition = it.positionInRoot().plus(Offset(x = 0f, y = yOffset))
            }
        }
    }, content = content)
}

@Composable
fun DropTargetIndicatorLine() {
    val state = LocalDragDropState.current

    val targetValue = LocalDensity.current.run {
        (state.currentDropTargetPosition ?: Offset.Zero) - Offset.Zero.copy(y = 48.dp.toPx())
    }
    val offset by animateOffsetAsState(targetValue = targetValue)
    if (state.isDragging) {
        Canvas(
            modifier = Modifier
                .padding(end = 20.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = offset.y
                }
        ) {
            drawLine(
                color = Color.Red,
                start = offset.copy(y = 0f),
                end = offset.copy(x = this.size.width, y = 0f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
