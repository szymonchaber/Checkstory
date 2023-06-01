package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEffect
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogLoadingState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogViewModel
import dev.szymonchaber.checkstory.checklist.catalog.recent.RecentChecklistsView
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.LoadingView
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.navigation.Routes

@Composable
@Destination(route = "home_screen", start = true)
fun ChecklistCatalogScreen(navigator: DestinationsNavigator) {
    trackScreenName("checklist_catalog")
    val viewModel = hiltViewModel<ChecklistCatalogViewModel>()
    var showMenu by remember { mutableStateOf(false) }
    AdvertScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.checkstory))
                },
                elevation = 12.dp,
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(ChecklistCatalogEvent.GetCheckstoryProClicked)
                        }) {
                            Text(text = stringResource(id = R.string.upgrade))
                        }
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(ChecklistCatalogEvent.AboutClicked)
                        }) {
                            Text(text = stringResource(id = R.string.about))
                        }
//                        DropdownMenuItem(onClick = {
//                            navigator.navigate("debug_screen")
//                        }) {
//                            Text(text = "Debug menu")
//                        }
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(ChecklistCatalogEvent.AccountClicked)
                        }) {
                            Text(text = "Account")
                        }
                    }
                }
            )
        }, content = {
            ChecklistCatalogView(viewModel, navigator)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = stringResource(R.string.new_template).uppercase(),
                        style = MaterialTheme.typography.button
                    )
                },
                onClick = {
                    viewModel.onEvent(ChecklistCatalogEvent.NewTemplateClicked)
                },
                icon = { Icon(Icons.Filled.Add, null) }
            )
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ChecklistCatalogView(
    viewModel: ChecklistCatalogViewModel,
    navigator: DestinationsNavigator
) {
    val state by viewModel.state.collectAsState(initial = ChecklistCatalogState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)

    var showUnassignedPaymentDialog by remember { mutableStateOf(false) }
    if (showUnassignedPaymentDialog) {
        UnassignedPaymentDialog(onDismiss = { showUnassignedPaymentDialog = false }) {
            viewModel.onEvent(ChecklistCatalogEvent.CreateAccountForPaymentClicked)
            showUnassignedPaymentDialog = false
        }
    }
    LaunchedEffect(effect) {
        when (val value = effect) {
            is ChecklistCatalogEffect.NavigateToOnboarding -> {
                navigator.navigate(Routes.onboardingScreen())
            }

            is ChecklistCatalogEffect.CreateAndNavigateToChecklist -> {
                navigator.navigate(Routes.newChecklistScreen(value.basedOn))
            }

            is ChecklistCatalogEffect.NavigateToChecklist -> {
                navigator.navigate(Routes.editChecklistScreen(value.checklistId))
            }
            is ChecklistCatalogEffect.NavigateToTemplateEdit -> {
                navigator.navigate(Routes.editTemplateScreen(value.templateId))
            }
            is ChecklistCatalogEffect.NavigateToTemplateHistory -> {
                navigator.navigate(Routes.checklistHistoryScreen(value.templateId))
            }

            is ChecklistCatalogEffect.NavigateToNewTemplate -> {
                navigator.navigate(Routes.newTemplateScreen())
            }

            is ChecklistCatalogEffect.NavigateToPaymentScreen -> {
                navigator.navigate(Routes.paymentScreen())
            }

            is ChecklistCatalogEffect.NavigateToAboutScreen -> {
                navigator.navigate(Routes.aboutScreen())
            }

            is ChecklistCatalogEffect.ShowUnassignedPaymentDialog -> {
                showUnassignedPaymentDialog = true
            }

            is ChecklistCatalogEffect.NavigateToAccountScreen -> {
                navigator.navigate(Routes.accountScreen())
            }

            null -> Unit
        }
    }

    val pullRefreshState = rememberPullRefreshState(state.isRefreshing, {
        viewModel.onEvent(ChecklistCatalogEvent.PulledToRefresh)
    })

    Box(Modifier.pullRefresh(pullRefreshState)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                RecentChecklistsView(state.recentChecklistsLoadingState, viewModel::onEvent)
            }
            templates(state, viewModel)
        }
        PullRefreshIndicator(state.isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

private fun LazyListScope.templates(
    state: ChecklistCatalogState,
    viewModel: ChecklistCatalogViewModel
) {
    item {
        SectionLabel(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.templates),
        )
    }
    when (val loadingState = state.templatesLoadingState) {
        ChecklistCatalogLoadingState.Loading -> {
            item {
                LoadingView()
            }
        }
        is ChecklistCatalogLoadingState.Success -> {
            if (loadingState.templates.isEmpty()) {
                item {
                    NoTemplatesView()
                }
            } else {
                items(loadingState.templates) {
                    TemplateView(it, viewModel::onEvent)
                }
            }
        }
    }
}

@Composable
fun NoTemplatesView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text(
            modifier = Modifier
                .padding(all = 24.dp)
                .align(alignment = Alignment.Center),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.templates_empty)
        )
    }
}

@Composable
fun UnassignedPaymentDialog(
    onDismiss: () -> Unit,
    onConfirmClicked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cloud synchronization is now available for PRO users!")
        },
        text = {
            Text("Create an account to keep your checklists backed up")
        },
        confirmButton = {
            TextButton(onClick = onConfirmClicked) {
                Text("Sign in")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        }
    )
}
