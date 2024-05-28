package dev.szymonchaber.checkstory.checklist.catalog.model

import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.data.preferences.OnboardingPreferences
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.usecase.CheckForUnassignedPaymentUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteChecklistUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetAllTemplatesUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.SynchronizeDataUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllTemplatesUseCase: GetAllTemplatesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val tracker: Tracker,
    private val onboardingPreferences: OnboardingPreferences,
    private val synchronizeDataUseCase: SynchronizeDataUseCase,
    private val checkForUnassignedPaymentUseCase: CheckForUnassignedPaymentUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
) : BaseViewModel<
        HomeEvent,
        HomeState,
        HomeEffect
        >(
    HomeState.initial
) {

    init {
        viewModelScope.launch {
            if (
                checkForUnassignedPaymentUseCase.isUnassignedPaymentPresent() &&
                onboardingPreferences.didShowOnboarding.first()
            ) {
                onEvent(HomeEvent.UnassignedPaymentPresent)
            }
        }
        viewModelScope.launch {
            onboardingPreferences.didShowOnboarding
                .flatMapLatest { didShowOnboarding ->
                    if (didShowOnboarding) {
                        HomeEvent.LoadChecklistCatalog
                    } else {
                        HomeEvent.GoToOnboarding
                    }.let(::flowOf)
                }
                .onEach(::onEvent)
                .collect()
        }
    }

    override fun buildMviFlow(eventFlow: Flow<HomeEvent>): Flow<Pair<HomeState?, HomeEffect?>> {
        return merge(
            eventFlow.handleLoadCatalog(),
            eventFlow.handleGoToOnboarding(),
            eventFlow.handleUnassignedPaymentPresent(),
            eventFlow.handleCreateAccountForPaymentClicked(),
            eventFlow.handleAccountClicked(),
            eventFlow.handleUseTemplateClicked(),
            eventFlow.handleRecentChecklistClicked(),
            eventFlow.handleRecentChecklistInTemplateClicked(),
            eventFlow.handleNewTemplateClicked(),
            eventFlow.handleEditTemplateClicked(),
            eventFlow.handleHistoryClicked(),
            eventFlow.handleGetProClicked(),
            eventFlow.handleAboutClicked(),
            eventFlow.handleRefreshCatalog(),
            eventFlow.handleDeleteTemplateConfirmed(),
            eventFlow.handleDeleteChecklistConfirmed()
        ).catch {
            Timber.e(it)
            FirebaseCrashlytics.getInstance().recordException(it)
        }
    }

    private fun Flow<HomeEvent>.handleLoadCatalog(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.LoadChecklistCatalog>()
            .flatMapLatest {
                combine(
                    getAllTemplatesUseCase.getAllTemplates(),
                    getCurrentUserUseCase.getCurrentUserFlow()
                ) { templates, user ->
                    ChecklistCatalogLoadingState.Success(
                        templates = templates,
                        canAddTemplate = canAddTemplate(user, templates)
                    )
                }
                    .onStart<ChecklistCatalogLoadingState> {
                        emit(ChecklistCatalogLoadingState.Loading)
                    }
                    .map {
                        state.value.copy(templatesLoadingState = it) to null
                    }
            }
    }

    private fun Flow<HomeEvent>.handleRefreshCatalog(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.PulledToRefresh>()
            .flatMapLatest {
                flow {
                    emit(state.first().copy(isRefreshing = true) to null)
                    synchronizeDataUseCase.synchronizeData()
                    emit(state.first().copy(isRefreshing = false) to null)
                }
            }
    }

    private fun Flow<HomeEvent>.handleGoToOnboarding(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.GoToOnboarding>()
            .map {
                state.first() to HomeEffect.NavigateToOnboarding
            }
    }

    private fun Flow<HomeEvent>.handleUnassignedPaymentPresent(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.UnassignedPaymentPresent>()
            .map {
                state.first() to HomeEffect.ShowUnassignedPaymentDialog
            }
    }

    private fun Flow<HomeEvent>.handleCreateAccountForPaymentClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.CreateAccountForPaymentClicked>()
            .mapLatest {
                state.first() to HomeEffect.NavigateToAccountScreen(true)
            }
    }

    private fun Flow<HomeEvent>.handleAccountClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.AccountClicked>()
            .mapLatest {
                state.first() to HomeEffect.NavigateToAccountScreen(false)
            }
    }

    private fun Flow<HomeEvent>.handleUseTemplateClicked(): Flow<Pair<HomeState?, HomeEffect?>> {
        return filterIsInstance<HomeEvent.UseTemplateClicked>()
            .onEach {
                tracker.logEvent("template_clicked")
            }
            .map { event ->
                null to HomeEffect.CreateAndNavigateToChecklist(basedOn = event.template.id)
            }
    }

    private fun Flow<HomeEvent>.handleRecentChecklistClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.RecentChecklistClicked>()
            .onEach {
                tracker.logEvent("recent_checklist_clicked")
            }
            .map {
                state.first() to HomeEffect.NavigateToChecklist(checklistId = it.checklistId)
            }
    }

    private fun Flow<HomeEvent>.handleRecentChecklistInTemplateClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.RecentChecklistClickedInTemplate>()
            .onEach {
                tracker.logEvent("recent_checklist_under_template_clicked")
            }
            .map {
                state.first() to HomeEffect.NavigateToChecklist(checklistId = it.checklistId)
            }
    }

    private fun Flow<HomeEvent>.handleEditTemplateClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.EditTemplateClicked>()
            .onEach {
                tracker.logEvent("catalog_edit_template_clicked")
            }
            .map {
                state.first() to HomeEffect.NavigateToTemplateEdit(templateId = it.templateId)
            }
    }

    private fun Flow<HomeEvent>.handleNewTemplateClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.NewTemplateClicked>()
            .onEach {
                tracker.logEvent("new_template_clicked")
            }
            .withSuccessState()
            .mapLatest { (state, _) ->
                val user = getCurrentUserUseCase.getCurrentUserFlow().first()
                val effect = if (canAddTemplate(user, state.templates)) {
                    HomeEffect.NavigateToNewTemplate
                } else {
                    HomeEffect.NavigateToPaymentScreen
                }
                _state.first() to effect
            }
    }

    private fun Flow<HomeEvent>.handleGetProClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.GetCheckstoryProClicked>()
            .onEach {
                tracker.logEvent("catalog_get_pro_option_clicked")
            }
            .mapLatest {
                _state.first() to HomeEffect.NavigateToPaymentScreen
            }
    }

    private fun Flow<HomeEvent>.handleAboutClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.AboutClicked>()
            .onEach {
                tracker.logEvent("about_clicked")
            }
            .mapLatest {
                _state.first() to HomeEffect.NavigateToAboutScreen
            }
    }

    private fun canAddTemplate(user: User, list: List<Template>): Boolean {
        return list.count() < MAX_FREE_TEMPLATES || user.isPaidUser
    }

    private fun Flow<HomeEvent>.handleHistoryClicked(): Flow<Pair<HomeState, HomeEffect?>> {
        return filterIsInstance<HomeEvent.TemplateHistoryClicked>()
            .onEach {
                tracker.logEvent("catalog_template_history_clicked")
            }
            .map {
                state.first() to HomeEffect.NavigateToTemplateHistory(templateId = it.templateId)
            }
    }

    private fun Flow<HomeEvent>.handleDeleteTemplateConfirmed(): Flow<Pair<HomeState?, HomeEffect?>> {
        return filterIsInstance<HomeEvent.DeleteTemplateConfirmed>()
            .onEach {
                tracker.logEvent("catalog_template_deleted")
            }
            .map {
                deleteTemplateUseCase.deleteTemplate(it.templateId)
                null to null // TODO maybe show a toast?
            }
    }

    private fun Flow<HomeEvent>.handleDeleteChecklistConfirmed(): Flow<Pair<HomeState?, HomeEffect?>> {
        return filterIsInstance<HomeEvent.DeleteChecklistConfirmed>()
            .onEach {
                tracker.logEvent("catalog_checklist_deleted")
            }
            .map {
                deleteChecklistUseCase.deleteChecklist(it.checklistId)
                null to null // TODO maybe show a toast?
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<ChecklistCatalogLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.templatesLoadingState }
                .filterIsInstance<ChecklistCatalogLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }

    companion object {

        internal const val MAX_FREE_TEMPLATES = 2
    }
}
