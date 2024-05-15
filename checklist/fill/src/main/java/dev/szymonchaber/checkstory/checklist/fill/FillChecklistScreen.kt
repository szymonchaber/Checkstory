package dev.szymonchaber.checkstory.checklist.fill

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEffect
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEvent
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistViewModel
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.dialog.ConfirmDeleteChecklistDialog
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.CheckedItemsRatio
import dev.szymonchaber.checkstory.design.views.ConfirmExitWithoutSavingDialog
import dev.szymonchaber.checkstory.design.views.DateFormatText
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.design.views.LinkifyText
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.design.views.Space
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch

@NavGraph<ExternalModuleGraph>
annotation class FillChecklistGraph

@OptIn(ExperimentalMaterialApi::class)
@Destination<FillChecklistGraph>(
    route = "fill_checklist_screen",
    start = true,
    deepLinks = [
        DeepLink(
            uriPattern = "app://checkstory/checklist/new/{createChecklistFrom}"
        ),
        DeepLink(
            uriPattern = "app://checkstory/checklist/fill/{checklistId}"
        )
    ]
)
@Composable
fun FillChecklistScreen(
    navigator: DestinationsNavigator,
    checklistId: ChecklistId?,
    createChecklistFrom: TemplateId?
) {
    trackScreenName("fill_checklist")
    val viewModel = hiltViewModel<FillChecklistViewModel>()
    checklistId?.let {
        LaunchedEffect(it) {
            viewModel.onEvent(FillChecklistEvent.LoadChecklist(it))
        }
    }
    createChecklistFrom?.let {
        LaunchedEffect(it) {
            viewModel.onEvent(FillChecklistEvent.CreateChecklistFromTemplate(it))
        }
    }

    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val notesInputFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (modalBottomSheetState.isVisible) {
            scope.launch {
                modalBottomSheetState.hide()
            }
        } else {
            viewModel.onEvent(FillChecklistEvent.BackClicked)
        }
    }

    val openConfirmDeleteDialog = remember { mutableStateOf(false) }
    if (openConfirmDeleteDialog.value) {
        ConfirmDeleteChecklistDialog(openConfirmDeleteDialog) {
            viewModel.onEvent(FillChecklistEvent.ConfirmDeleteChecklistClicked)
            openConfirmDeleteDialog.value = false
        }
    }

    val openConfirmExitDialog = remember { mutableStateOf(false) }
    if (openConfirmExitDialog.value) {
        ConfirmExitWithoutSavingDialog(openConfirmExitDialog) {
            viewModel.onEvent(FillChecklistEvent.ConfirmExitClicked)
            openConfirmExitDialog.value = false
        }
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is FillChecklistEffect.NavigateToEditTemplate -> {
                    navigator.navigate(Routes.editTemplateScreen(it.templateId))
                }

                FillChecklistEffect.CloseScreen -> {
                    navigator.navigateUp()
                }

                is FillChecklistEffect.ShowNotesEditShelf -> {
                    scope.launch {
                        modalBottomSheetState.show()
                    }
                    scope.launch {
                        notesInputFocusRequester.requestFocus()
                    }
                }

                is FillChecklistEffect.ShowConfirmDeleteDialog -> {
                    openConfirmDeleteDialog.value = true
                }

                is FillChecklistEffect.ShowConfirmExitDialog -> {
                    openConfirmExitDialog.value = true
                }
            }
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (val loadingState = state) {
                    is FillChecklistState.Ready -> {
                        var textFieldValue by remember {
                            val notes = loadingState.checklist.notes
                            mutableStateOf(TextFieldValue(notes, selection = TextRange(notes.length)))
                        }
                        OutlinedTextField(
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(notesInputFocusRequester)
                                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
                            label = { Text(text = "Checklist name") },
                            value = textFieldValue,
                            onValueChange = {
                                textFieldValue = it
                                viewModel.onEvent(FillChecklistEvent.NotesChanged(it.text))
                            },
                        )
                        IconButton(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                scope.launch {
                                    focusManager.clearFocus()
                                }
                                scope.launch {
                                    modalBottomSheetState.hide()
                                }
                            }) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        }
                    }

                    else -> {
                        Box(Modifier.size(1.dp))
                    }
                }
            }
        },
        sheetState = modalBottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        FillChecklistScaffold(viewModel, state)
    }
}

@Composable
private fun FillChecklistScaffold(
    viewModel: FillChecklistViewModel,
    state: FillChecklistState
) {
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.fill_checklist))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(FillChecklistEvent.BackClicked)
                    }) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                },
                elevation = 12.dp,
                actions = {
                    when (state) {
                        FillChecklistState.Loading -> Unit
                        is FillChecklistState.Ready -> {
                            if (!state.isNew) {
                                IconButton(onClick = {
                                    viewModel.onEvent(FillChecklistEvent.DeleteChecklistClicked)
                                }) {
                                    Icon(Icons.Filled.Delete, "", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            )
        },
        content = {
            when (state) {
                FillChecklistState.Loading -> {
                    FullSizeLoadingView()
                }

                is FillChecklistState.Ready -> {
                    FillChecklistView(state.checklist, viewModel::onEvent)
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = stringResource(R.string.save).uppercase(),
                        style = MaterialTheme.typography.button
                    )
                },
                onClick = {
                    viewModel.onEvent(FillChecklistEvent.SaveChecklistClicked)
                },
                icon = {
                    Icon(Icons.Filled.Check, null)
                }
            )
        }
    )
}

private val nestedPaddingStart = 12.dp

@Composable
fun FillChecklistView(checklist: Checklist, eventCollector: (FillChecklistEvent) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            ChecklistInfo(checklist, eventCollector)
        }
        items(checklist.items, key = { it.id.id }) {
            Box(Modifier.padding(start = 8.dp)) {
                TaskSection(task = it, paddingStart = nestedPaddingStart, eventCollector = eventCollector)
            }
        }
    }
}

@Composable
private fun ChecklistInfo(checklist: Checklist, eventCollector: (FillChecklistEvent) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DateFormatText(
                localDateTime = checklist.createdAt
            )
            CheckedItemsRatio(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                checklist = checklist,
            )
        }
        Space(8.dp)
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            text = checklist.title,
        )
        if (checklist.description.isNotEmpty()) {
            Space(8.dp)
            SectionLabel(
                modifier = Modifier.padding(start = 16.dp),
                text = "Additional instructions",
            )
            Space(4.dp)
            LinkifyText(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = checklist.description
            )
        }
        Space(8.dp)
        Divider()
        Space(8.dp)
        ChecklistName(checklist, eventCollector)
        Space(8.dp)
        SectionLabel(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.tasks),
        )
    }
}

@Composable
private fun ChecklistName(
    checklist: Checklist,
    eventCollector: (FillChecklistEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp)
            .clickable {
                eventCollector(FillChecklistEvent.NotesClicked)
            }
            .border(
                1.dp,
                color = Color(0xFF9E9E9E),
                RoundedCornerShape(4.dp)
            )
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.clickable {
                eventCollector(FillChecklistEvent.NotesClicked)
            },
            text = checklist.notes.takeUnless { it.isEmpty() } ?: "Name this checklist, e.g. 'Client X'"
        )
    }
}

@Composable
fun TaskSection(
    task: Task,
    paddingStart: Dp,
    nestingLevel: Int = 1,
    isLastChild: Boolean = true,
    collapsedByDefault: Boolean = false,
    eventCollector: (FillChecklistEvent) -> Unit,
) {
    Column {
        val shouldIncludeIcon = task.children.isNotEmpty()
        var isCollapsed by remember(collapsedByDefault) { mutableStateOf(collapsedByDefault) }
        val endPadding = if (shouldIncludeIcon) 8.dp else 44.dp
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
                        .width(nestedPaddingStart)
                )
            }
            TaskView(
                modifier = Modifier.padding(end = endPadding),
                task = task,
                onCheckedChange = {
                    eventCollector(FillChecklistEvent.CheckChanged(task, it))
                }
            ) {
                if (shouldIncludeIcon) {
                    IconButton(
                        onClick = { isCollapsed = !isCollapsed }) {
                        val rotationDegrees = if (isCollapsed) 0f else 180f
                        Icon(
                            modifier = Modifier.rotate(rotationDegrees),
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
            }
        }
        val paddingMultiplier = if (nestingLevel == 1) {
            2
        } else {
            3
        }
        AnimatedVisibility(visible = !isCollapsed) {
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
                        .onGloballyPositioned {
                            columnHeightDp = with(localDensity) { it.size.height.toDp() }
                        }) {
                    task.children.forEachIndexed { index, child ->
                        TaskSection(
                            task = child,
                            paddingStart = nestedPaddingStart,
                            nestingLevel = nestingLevel + 1,
                            isLastChild = task.children.lastIndex == index,
                            collapsedByDefault = false,
                            eventCollector
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
fun Modifier.focusOnEntry(ignoreImeVisibility: Boolean = false) = composed {
    val imeVisible = WindowInsets.isImeVisible
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(true) {
        if (ignoreImeVisibility || imeVisible) {
            focusRequester.requestFocus()
        }
    }

    focusRequester(focusRequester = focusRequester)
}
