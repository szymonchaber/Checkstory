@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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
import timber.log.Timber
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

val LocalRecentlyAddedUnconsumedItem = compositionLocalOf {
    RecentlyAddedUnconsumedItem()
}

class RecentlyAddedUnconsumedItem {

    var item by mutableStateOf<ViewTemplateCheckboxKey?>(null)
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
                    val recentlyAddedUnconsumedItem = remember {
                        RecentlyAddedUnconsumedItem()
                    }
                    LaunchedEffect(key1 = loadingState.mostRecentlyAddedItem) {
                        recentlyAddedUnconsumedItem.item = loadingState.mostRecentlyAddedItem
                    }
                    CompositionLocalProvider(
                        LocalRecentlyAddedUnconsumedItem provides recentlyAddedUnconsumedItem,
                        LocalDragDropState provides rememberDragDropState()
                    ) {
                        EditTemplateView(loadingState, viewModel::onEvent)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTemplateView(
    success: TemplateLoadingState.Success,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val dragDropState = LocalDragDropState.current
    LaunchedEffect(dragDropState.isDragging) {
        val data = if (!dragDropState.isDragging) {
            dragDropState.dataToDrop
        } else {
            null
        }
        data?.let {
            dragDropState.dataToDrop = null
            dragDropState.checkboxViewId = null
            dragDropState.currentDropTarget?.invoke(it)
        }
    }
    val recentlyAddedItem = LocalRecentlyAddedUnconsumedItem.current
    LaunchedEffect(recentlyAddedItem.item) {
        recentlyAddedItem.item?.let { newItem ->
            val isNewItemNotVisible =
                dragDropState.lazyListState.layoutInfo.visibleItemsInfo.none { it.key == newItem }
            if (isNewItemNotVisible) {
                dragDropState.lazyListState.animateScrollToItem(success.unwrappedCheckboxes.indexOfFirst { it.first.viewKey == newItem } + 1)
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        val template = success.checklistTemplate
        LazyColumn(
            Modifier
                .detectLazyListReorder()
                .fillMaxSize(),
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
                    item.viewId
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
                    CommonCheckbox(
                        checkbox = checkbox,
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
                        placeTargetLineOnTop = true,
                        onDataDropped = { taskKey ->
                            eventCollector(EditTemplateEvent.CheckboxMovedToBottom(taskKey))
                        }
                    )
                }

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
                val task by remember(success.unwrappedCheckboxes, dragDropState.checkboxViewId) {
                    derivedStateOf {
                        success.unwrappedCheckboxes
                            .find {
                                it.first.viewId == dragDropState.checkboxViewId
                            }
                    }
                }
                task?.let { (foundTask, _) ->
                    LaunchedEffect(key1 = foundTask) {
                        dragDropState.dataToDrop = foundTask.viewKey
                    }
                    CheckboxItem(
                        title = foundTask.title,
                        placeholder = foundTask.placeholderTitle,
                        isFunctional = false,
                        focusRequester = remember { FocusRequester() },
                        onTitleChange = {},
                        onAddSubtask = {},
                        onDeleteClick = {},
                        acceptChildren = false
                    )
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
    nestingLevel: Int,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    val taskTopPadding = 8.dp
    val focusRequester = remember { FocusRequester() }
    Box(Modifier.height(IntrinsicSize.Min)) {
        val acceptChildren = nestingLevel < 3
        CheckboxItem(
            modifier = Modifier
//                .drawFolderStructure(nestingLevel, paddingStart, taskTopPadding) TODO decide if this should stay
                .padding(top = taskTopPadding, start = paddingStart),
            title = checkbox.title,
            placeholder = checkbox.placeholderTitle,
            focusRequester = focusRequester,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            },
            onAddSubtask = {
                eventCollector(EditTemplateEvent.ChildItemAdded(checkbox.viewKey))
            },
            onDeleteClick = {
                eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
            },
            acceptChildren = acceptChildren
        )
        Receptacles(
            modifier = Modifier.padding(top = taskTopPadding, start = paddingStart),
            acceptChildren = acceptChildren,
            forCheckbox = checkbox.viewKey,
            onSiblingTaskDropped = { siblingTask ->
                eventCollector(EditTemplateEvent.SiblingMovedBelow(checkbox.viewKey, siblingTask))
            },
            onChildTaskDropped = { childTask ->
                eventCollector(EditTemplateEvent.ChildMovedBelow(checkbox.viewKey, childTask))
            }
        )
    }
    val recentlyAddedItem = LocalRecentlyAddedUnconsumedItem.current
    LaunchedEffect(recentlyAddedItem.item) {
        if (checkbox.viewKey == recentlyAddedItem.item) {
            focusRequester.requestFocus()
            recentlyAddedItem.item = null
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
    val id: Long,
    val parentKey: ViewTemplateCheckboxKey?,
    val isNew: Boolean
) : Parcelable {
    fun hasKeyInAncestors(key: ViewTemplateCheckboxKey): Boolean {
        return parentKey == key || parentKey?.hasKeyInAncestors(key) ?: false
    }
}

val ViewTemplateCheckbox.viewKey: ViewTemplateCheckboxKey
    get() {
        return ViewTemplateCheckboxKey(
            id.id,
            parentViewKey,
            this is ViewTemplateCheckbox.New
        )
    }

@Parcelize
data class ViewTemplateCheckboxId(
    val viewId: Long,
    val isNew: Boolean
) : Parcelable

val ViewTemplateCheckbox.viewId: ViewTemplateCheckboxId
    get() {
        return ViewTemplateCheckboxId(id.id, this is ViewTemplateCheckbox.New)
    }

val ViewTemplateCheckboxKey.viewId: ViewTemplateCheckboxId
    get() {
        return ViewTemplateCheckboxId(
            id,
            isNew
        )
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
fun rememberDragDropState(lazyListState: LazyListState = rememberLazyListState()): DragDropState {
    return remember {
        DragDropState(lazyListState = lazyListState)
    }
}

@Composable
private fun Receptacles(
    forCheckbox: ViewTemplateCheckboxKey?,
    onSiblingTaskDropped: (ViewTemplateCheckboxKey) -> Unit,
    onChildTaskDropped: (ViewTemplateCheckboxKey) -> Unit,
    modifier: Modifier = Modifier,
    acceptChildren: Boolean,
) {
    Row(modifier.fillMaxSize()) {
        DropTarget(
            modifier = Modifier
                .fillMaxHeight()
                .then {
                    if (acceptChildren) {
                        width(24.dp)
                    } else {
                        fillMaxWidth()
                    }
                },
            key = forCheckbox,
            onDataDropped = { siblingTask ->
                onSiblingTaskDropped(siblingTask)
            }
        )
        if (acceptChildren) {
            DropTarget(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                key = forCheckbox,
                onDataDropped = { childTask ->
                    onChildTaskDropped(childTask)
                }
            )
        }
    }
}

fun Modifier.then(modifier: Modifier.() -> Modifier): Modifier {
    return then(modifier(Modifier))
}


@Composable
fun DropTarget(
    modifier: Modifier,
    onDataDropped: (ViewTemplateCheckboxKey) -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {},
    placeTargetLineOnTop: Boolean = false,
    key: ViewTemplateCheckboxKey? = null
) {
    val dragInfo = LocalDragDropState.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    val density = LocalDensity.current

    fun canReceive(viewTemplateCheckboxKey: ViewTemplateCheckboxKey?): Boolean {

        return let(key, viewTemplateCheckboxKey) { current, other ->
            Timber.d(
                """
                Can receive based on comparison: ${current != other}
                Can receive based on parent: ${!other.hasKeyInAncestors(current)}
                This key: $key
                checkedKey: $viewTemplateCheckboxKey
            """.trimIndent()
            )
            current != other && !current.hasKeyInAncestors(other)
        } ?: true
    }

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            val isCurrentDropTarget =
                rect.contains(dragPosition + dragOffset + Offset(0f, density.run { 96.dp.toPx() }))
            if (isCurrentDropTarget && canReceive(dragInfo.dataToDrop)) {
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


internal suspend fun PointerInputScope.detectDrag(
    down: PointerId,
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    awaitPointerEventScope {
        if (
            drag(down) {
                onDrag(it, it.positionChange())
                it.consume()
            }
        ) {
            // consume up if we quit drag gracefully with the up
            currentEvent.changes.forEach {
                if (it.changedToUp()) it.consume()
            }
            onDragEnd()
        } else {
            onDragCancel()
        }
    }
}

fun Modifier.detectLazyListReorder(): Modifier {
    return composed {
        val scope = rememberCoroutineScope()
        var overscrollJob by remember { mutableStateOf<Job?>(null) }
        val dragDropState = LocalDragDropState.current

        pointerInput(Unit) {
            forEachGesture {
                val dragStart = dragDropState.interactions.receive()
                val down = awaitPointerEventScope {
                    currentEvent.changes.fastFirstOrNull { it.id == dragStart.id }
                }
                if (down != null) {
                    dragDropState.onDragStart(down.position)
                    dragStart.offset?.apply {
                        dragDropState.onDrag(this)
                    }
                    detectDrag(
                        down.id,
                        onDragEnd = {
                            dragDropState.onDragInterrupted()
                        },
                        onDragCancel = {
                            dragDropState.onDragInterrupted()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragDropState.onDrag(dragAmount)

                            if (overscrollJob?.isActive == true) {
                                return@detectDrag
                            }

                            dragDropState
                                .checkForOverScroll()
                                .takeIf { it != 0f }
                                ?.let {
                                    overscrollJob =
                                        scope.launch { dragDropState.lazyListState.scrollBy(it) }
                                }
                                ?: run { overscrollJob?.cancel() }
                        })
                }
            }
        }
    }
}

fun <T, U, R> let(first: T?, second: U?, block: (T, U) -> R): R? {
    return first?.let { nonNullFirst ->
        second?.let { nonNullSecond ->
            block(nonNullFirst, nonNullSecond)
        }
    }
}
