package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEffect
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogEvent
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogLoadingState
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogViewModel
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.theme.Primary
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.LoadingView
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.design.views.Space
import dev.szymonchaber.checkstory.navigation.Routes

@NavGraph<ExternalModuleGraph>
annotation class HomeGraph

@Composable
@Destination<HomeGraph>(route = "home_screen", start = true)
fun HomeScreen(navigator: DestinationsNavigator) {
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
                        if (!ActiveUser.current.isPaidUser) {
                            DropdownMenuItem(onClick = {
                                viewModel.onEvent(ChecklistCatalogEvent.GetCheckstoryProClicked)
                            }) {
                                Text(text = stringResource(id = R.string.upgrade))
                            }
                        }
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(ChecklistCatalogEvent.AccountClicked)
                        }) {
                            Text(text = "Account")
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
                    }
                }
            )
        },
        content = {
            ChecklistCatalogView(viewModel, navigator)
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ChecklistCatalogView(
    viewModel: ChecklistCatalogViewModel,
    navigator: DestinationsNavigator
) {
    val state by viewModel.state.collectAsState()

    var showUnassignedPaymentDialog by remember { mutableStateOf(false) }
    if (showUnassignedPaymentDialog) {
        UnassignedPaymentDialog(onDismiss = { showUnassignedPaymentDialog = false }) {
            viewModel.onEvent(ChecklistCatalogEvent.CreateAccountForPaymentClicked)
            showUnassignedPaymentDialog = false
        }
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is ChecklistCatalogEffect.NavigateToOnboarding -> {
                    navigator.navigate(Routes.onboardingScreen())
                }

                is ChecklistCatalogEffect.CreateAndNavigateToChecklist -> {
                    navigator.navigate(Routes.newChecklistScreen(it.basedOn))
                }

                is ChecklistCatalogEffect.NavigateToChecklist -> {
                    navigator.navigate(Routes.editChecklistScreen(it.checklistId))
                }

                is ChecklistCatalogEffect.NavigateToTemplateEdit -> {
                    navigator.navigate(Routes.editTemplateScreen(it.templateId))
                }

                is ChecklistCatalogEffect.NavigateToTemplateHistory -> {
                    navigator.navigate(Routes.checklistHistoryScreen(it.templateId))
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
                    navigator.navigate(
                        Routes.accountScreen(
                            triggerPurchaseRestoration = it.triggerPurchaseRestoration
                        )
                    )
                }
            }
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
//            item {
//                RecentChecklistsView(state.recentChecklistsLoadingState, viewModel::onEvent)
//            }
            item {
                TemplatesHeader(state.templatesLoadingState, viewModel::onEvent)
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
                            NoTemplatesView(viewModel::onEvent)
                        }
                    } else {
                        itemsIndexed(loadingState.templates, key = { _, item -> item.id }) { index, item ->
                            TemplateView(item, viewModel::onEvent)
                            if (index < loadingState.templates.lastIndex) {
                                Divider()
                            }
                        }
                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    modifier = Modifier.align(Alignment.Center),
                                    onClick = {
                                        viewModel.onEvent(ChecklistCatalogEvent.NewTemplateClicked)
                                    }) {
                                    Text(
                                        text = stringResource(R.string.new_checklist).uppercase(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        PullRefreshIndicator(state.isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun TemplatesHeader(state: ChecklistCatalogLoadingState, onEvent: (ChecklistCatalogEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(R.string.templates),
        )

        when (state) {
            is ChecklistCatalogLoadingState.Success -> {
                val currentCount = remember(state) {
                    state.templates.count()
                }
                Space(8.dp)
                if (ActiveUser.current.isPaidUser) {
                    PaidTemplateCounter(currentCount)
                } else {
                    FreeTemplateCounter(currentCount, onEvent)
                }
            }

            ChecklistCatalogLoadingState.Loading -> Unit
        }
    }
}

@Composable
private fun PaidTemplateCounter(currentCount: Int) {
    Text(
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
        text = "$currentCount /"
    )
    Text(
        text = "∞"
    )
}

@Composable
private fun RowScope.FreeTemplateCounter(
    currentCount: Int,
    onEvent: (ChecklistCatalogEvent) -> Unit
) {
    Text(
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
        text = "$currentCount / ${ChecklistCatalogViewModel.MAX_FREE_TEMPLATES} free"
    )
    Space(4.dp)
    Text(
        modifier = Modifier
            .background(Primary)
            .clickable {
                onEvent(ChecklistCatalogEvent.GetCheckstoryProClicked)
            }
            .padding(4.dp),
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
        color = Color.White,
        text = "PRO: unlimited"
    )
}

@Composable
fun NoTemplatesView(onEvent: (ChecklistCatalogEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(all = 24.dp),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.templates_empty)
        )
        Button(
            onClick = {
                onEvent(ChecklistCatalogEvent.NewTemplateClicked)
            }) {
            Text(
                text = stringResource(R.string.new_checklist).uppercase(),
            )
        }
    }
}

@Composable
fun UnassignedPaymentDialog(
    onDismiss: () -> Unit,
    onConfirmClicked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
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
