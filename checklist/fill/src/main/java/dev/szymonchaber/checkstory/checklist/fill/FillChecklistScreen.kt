package dev.szymonchaber.checkstory.checklist.fill

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.fill.model.ChecklistLoadingState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEffect
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEvent
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistViewModel
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.ConfirmExitWithoutSavingDialog
import dev.szymonchaber.checkstory.design.views.DeleteButton
import dev.szymonchaber.checkstory.design.views.FullSizeLoadingView
import dev.szymonchaber.checkstory.design.views.LinkifyText
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Destination(
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
    createChecklistFrom: ChecklistTemplateId?
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

    val state = viewModel.state.collectAsState(initial = FillChecklistState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is FillChecklistEffect.NavigateToEditTemplate -> {
                navigator.navigate(Routes.editChecklistTemplateScreen(value.templateId))
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
            null -> Unit
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            val loadingState = state.value.checklistLoadingState
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (loadingState is ChecklistLoadingState.Success) {
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
                        label = { Text(text = stringResource(R.string.notes)) },
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
                } else {
                    Box(Modifier.size(1.dp))
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
    state: State<FillChecklistState>
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
                    IconButton(onClick = {
                        viewModel.onEvent(FillChecklistEvent.EditTemplateClicked)
                    }) {
                        Icon(Icons.Filled.Edit, "", tint = Color.White)
                    }
                }
            )
        }, content = {
            when (val loadingState = state.value.checklistLoadingState) {
                ChecklistLoadingState.Loading -> {
                    FullSizeLoadingView()
                }
                is ChecklistLoadingState.Success -> {
                    FillChecklistView(loadingState.checklist, viewModel::onEvent)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(FillChecklistEvent.SaveChecklistClicked)
            }) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
            }
        }
    )
}

private val nestedPaddingStart = 12.dp

@Composable
fun FillChecklistView(checklist: Checklist, eventCollector: (FillChecklistEvent) -> Unit) {
    LazyColumn {
        item {
            ChecklistInfo(checklist.title, checklist.description)
        }
        items(checklist.items, key = { it.id.id }) {
            Box(Modifier.padding(start = 8.dp)) {
                CheckboxSection(checkbox = it, paddingStart = nestedPaddingStart, eventCollector = eventCollector)
            }
        }
        item {
            NotesSection(checklist, eventCollector)
            Box(Modifier.fillMaxWidth()) {
                DeleteButton(
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .padding(top = 24.dp, bottom = 96.dp)
                ) {
                    eventCollector(FillChecklistEvent.DeleteChecklistClicked)
                }
            }
        }
    }
}

@Composable
private fun ChecklistInfo(title: String, description: String) {
    SectionLabel(
        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        text = stringResource(R.string.title),
    )
    Text(
        modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp),
        text = title,
        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Normal)
    )
    if (description.isNotEmpty()) {
        SectionLabel(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            text = stringResource(R.string.description),
        )
        LinkifyText(
            modifier = Modifier.padding(start = 16.dp, top = 2.dp, end = 16.dp),
            text = description
        )
    }
    SectionLabel(
        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
        text = stringResource(R.string.items),
    )
}

@Composable
private fun NotesSection(
    checklist: Checklist,
    eventCollector: (FillChecklistEvent) -> Unit
) {
    SectionLabel(
        modifier = Modifier
            .padding(top = 4.dp)
            .padding(horizontal = 16.dp),
        text = stringResource(R.string.notes)
    )
    Row(
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
        LinkifyText(
            text = checklist.notes
        )
    }
}


@Composable
fun CheckboxSection(
    checkbox: Checkbox,
    paddingStart: Dp,
    nestingLevel: Int = 1,
    isLastChild: Boolean = true,
    collapsedByDefault: Boolean = true,
    eventCollector: (FillChecklistEvent) -> Unit,
) {
    Column {
        val shouldIncludeIcon = checkbox.children.isNotEmpty()
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
            CheckboxItem(
                modifier = Modifier.padding(end = endPadding),
                checkbox = checkbox,
                onCheckedChange = {
                    eventCollector(FillChecklistEvent.CheckChanged(checkbox, it))
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
                    checkbox.children.forEachIndexed { index, child ->
                        CheckboxSection(
                            checkbox = child,
                            paddingStart = nestedPaddingStart,
                            nestingLevel = nestingLevel + 1,
                            isLastChild = checkbox.children.lastIndex == index,
                            collapsedByDefault = true,
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
