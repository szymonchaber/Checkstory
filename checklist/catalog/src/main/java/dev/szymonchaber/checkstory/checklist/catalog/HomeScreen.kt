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
import androidx.compose.material.Card
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.checklist.catalog.model.ChecklistCatalogLoadingState
import dev.szymonchaber.checkstory.checklist.catalog.model.HomeEffect
import dev.szymonchaber.checkstory.checklist.catalog.model.HomeEvent
import dev.szymonchaber.checkstory.checklist.catalog.model.HomeViewModel
import dev.szymonchaber.checkstory.common.trackScreenName
import dev.szymonchaber.checkstory.design.ActiveUser
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.design.theme.Primary
import dev.szymonchaber.checkstory.design.views.AdvertScaffold
import dev.szymonchaber.checkstory.design.views.LoadingView
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.design.views.Space
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.navigation.Routes
import javax.inject.Inject

@NavGraph<ExternalModuleGraph>
annotation class HomeGraph

@Composable
@Destination<HomeGraph>(route = "home_screen", start = true)
fun HomeScreen(navigator: DestinationsNavigator) {
    trackScreenName("checklist_catalog")
    val viewModel = hiltViewModel<HomeViewModel>()
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
                        val user = ActiveUser.current
                        (user as? User.LoggedIn)?.email?.let {
                            Text(modifier = Modifier.padding(all = 16.dp), text = it)
                        }
                        if (!user.isPaidUser) {
                            DropdownMenuItem(onClick = {
                                viewModel.onEvent(HomeEvent.GetCheckstoryProClicked)
                            }) {
                                Text(text = stringResource(id = R.string.upgrade))
                            }
                        }
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(HomeEvent.AccountClicked)
                        }) {
                            Text(text = "Account")
                        }
                        DropdownMenuItem(onClick = {
                            viewModel.onEvent(HomeEvent.AboutClicked)
                        }) {
                            Text(text = stringResource(id = R.string.about))
                        }
//                        DebugTools()
//                        DropdownMenuItem(onClick = {
//                            navigator.navigate(Direction("debug_screen"))
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
        backgroundColor = Color(0xFFF0F0F0)
    )
}

@Composable
fun DebugTools() {
    val debugToolsViewModel = hiltViewModel<DebugToolsViewModel>()
    DropdownMenuItem(onClick = {
        debugToolsViewModel.logoutFirebase()
    }) {
        Text("Logout Firebase")
    }
}

@HiltViewModel
internal class DebugToolsViewModel @Inject constructor(

) : ViewModel() {

    fun logoutFirebase() {
        FirebaseAuth.getInstance().signOut()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ChecklistCatalogView(
    viewModel: HomeViewModel,
    navigator: DestinationsNavigator
) {
    val state by viewModel.state.collectAsState()

    var showUnassignedPaymentDialog by remember { mutableStateOf(false) }
    if (showUnassignedPaymentDialog) {
        UnassignedPaymentDialog(onDismiss = { showUnassignedPaymentDialog = false }) {
            viewModel.onEvent(HomeEvent.CreateAccountForPaymentClicked)
            showUnassignedPaymentDialog = false
        }
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is HomeEffect.NavigateToOnboarding -> {
                    navigator.navigate(Routes.onboardingScreen())
                }

                is HomeEffect.CreateAndNavigateToChecklist -> {
                    navigator.navigate(Routes.newChecklistScreen(it.basedOn))
                }

                is HomeEffect.NavigateToChecklist -> {
                    navigator.navigate(Routes.editChecklistScreen(it.checklistId))
                }

                is HomeEffect.NavigateToTemplateEdit -> {
                    navigator.navigate(Routes.editTemplateScreen(it.templateId))
                }

                is HomeEffect.NavigateToTemplateHistory -> {
                    navigator.navigate(Routes.checklistHistoryScreen(it.templateId))
                }

                is HomeEffect.NavigateToNewTemplate -> {
                    navigator.navigate(Routes.newTemplateScreen())
                }

                is HomeEffect.NavigateToPaymentScreen -> {
                    navigator.navigate(Routes.paymentScreen())
                }

                is HomeEffect.NavigateToAboutScreen -> {
                    navigator.navigate(Routes.aboutScreen())
                }

                is HomeEffect.ShowUnassignedPaymentDialog -> {
                    showUnassignedPaymentDialog = true
                }

                is HomeEffect.NavigateToAccountScreen -> {
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
        viewModel.onEvent(HomeEvent.PulledToRefresh)
    })

    val loadingState = remember(state) {
        state.templatesLoadingState
    }

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
            if (loadingState is ChecklistCatalogLoadingState.Success && !loadingState.canAddTemplate) {
                item {
                    FreeLimitReachedBanner(viewModel::onEvent)
                }
            }
            item {
                TemplatesHeader(loadingState, viewModel::onEvent)
            }
            when (loadingState) {
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
                        }
                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    modifier = Modifier.align(Alignment.Center),
                                    onClick = {
                                        viewModel.onEvent(HomeEvent.NewTemplateClicked)
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
private fun TemplatesHeader(state: ChecklistCatalogLoadingState, onEvent: (HomeEvent) -> Unit) {
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
    onEvent: (HomeEvent) -> Unit
) {
    Text(
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
        text = "$currentCount / ${HomeViewModel.MAX_FREE_TEMPLATES} free"
    )
    Space(4.dp)
    Text(
        modifier = Modifier
            .background(Primary)
            .clickable {
                onEvent(HomeEvent.GetCheckstoryProClicked)
            }
            .padding(4.dp),
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
        color = Color.White,
        text = "PRO: unlimited"
    )
}

@Composable
fun NoTemplatesView(onEvent: (HomeEvent) -> Unit) {
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
                onEvent(HomeEvent.NewTemplateClicked)
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

@Preview
@Composable
fun FreeLimitReachedBanner(
    onEvent: (HomeEvent) -> Unit = {},
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Free template limit reached",
                style = MaterialTheme.typography.h6
            )
            Space(size = 4.dp)
            Text(
                text = "Upgrade to PRO to create more templates",
                style = MaterialTheme.typography.body2
            )
            Space(size = 8.dp)
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    onEvent(HomeEvent.GetCheckstoryProClicked)
                }
            ) {
                Text("Upgrade now")
            }
        }
    }
}