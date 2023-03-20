@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
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
import dev.szymonchaber.checkstory.checklist.template.model.OnboardingPlaceholders
import dev.szymonchaber.checkstory.checklist.template.model.TemplateLoadingState
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
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
        FloatingDraggable(success)
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

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

fun Modifier.then(modifier: Modifier.() -> Modifier): Modifier {
    return then(modifier(Modifier))
}
