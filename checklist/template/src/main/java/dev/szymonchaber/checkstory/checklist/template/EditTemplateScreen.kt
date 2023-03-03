@file:OptIn(ExperimentalMaterialApi::class)

package dev.szymonchaber.checkstory.checklist.template

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
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
        EditTemplateScaffold(templateId == null, state, viewModel)
    }
}

val RecentlyAddedUnconsumedItem = compositionLocalOf<ViewTemplateCheckboxKey?> {
    null
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
                    var recentlyAddedUnconsumedItem by remember {
                        mutableStateOf<ViewTemplateCheckboxKey?>(null)
                    }
                    LaunchedEffect(key1 = loadingState.mostRecentlyAddedItem) {
                        recentlyAddedUnconsumedItem = loadingState.mostRecentlyAddedItem
                    }
                    CompositionLocalProvider(RecentlyAddedUnconsumedItem provides recentlyAddedUnconsumedItem) {
                        EditTemplateView(
                            loadingState.checklistTemplate,
                            loadingState.onboardingPlaceholders,
                            loadingState.checkboxes,
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

private val nestedPaddingStart = 16.dp

@Composable
fun EditTemplateView(
    checklistTemplate: ChecklistTemplate,
    onboardingPlaceholders: OnboardingPlaceholders?,
    checkboxes: List<ViewTemplateCheckbox>,
    eventCollector: (EditTemplateEvent) -> Unit,
    onAddedItemConsumed: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            ChecklistTemplateDetails(checklistTemplate, onboardingPlaceholders, eventCollector)
        }
        items(
            items = checkboxes,
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
private fun CommonCheckbox(
    checkbox: ViewTemplateCheckbox,
    paddingStart: Dp,
    isLastChild: Boolean,
    nestingLevel: Int = 1,
    onAddedItemConsumed: () -> Unit,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    Column {
        Row(Modifier.height(IntrinsicSize.Min)) {
            if (nestingLevel > 1) {
                val heightFraction = if (!isLastChild) 1f else 0.52f
                Box(
                    modifier = Modifier
                        .fillMaxHeight(heightFraction)
                        .background(Color.Gray)
                        .width(2.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .background(Color.Gray)
                        .height(2.dp)
                        .width(paddingStart)
                )
            }
            val focusRequester = remember { FocusRequester() }
            CheckboxItem(
                modifier = Modifier.padding(top = 8.dp),
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
private fun ChecklistTemplateDetails(
    checklistTemplate: ChecklistTemplate,
    onboardingPlaceholders: OnboardingPlaceholders?,
    eventCollector: (EditTemplateEvent) -> Unit
) {
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
