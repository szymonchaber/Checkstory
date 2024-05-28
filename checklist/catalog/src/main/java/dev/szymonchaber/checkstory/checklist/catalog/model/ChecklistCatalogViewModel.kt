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
class ChecklistCatalogViewModel @Inject constructor(
    private val getAllTemplatesUseCase: GetAllTemplatesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val tracker: Tracker,
    private val onboardingPreferences: OnboardingPreferences,
    private val synchronizeDataUseCase: SynchronizeDataUseCase,
    private val checkForUnassignedPaymentUseCase: CheckForUnassignedPaymentUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
) : BaseViewModel<
        ChecklistCatalogEvent,
        ChecklistCatalogState,
        ChecklistCatalogEffect
        >(
    ChecklistCatalogState.initial
) {

    init {
        viewModelScope.launch {
            if (
                checkForUnassignedPaymentUseCase.isUnassignedPaymentPresent() &&
                onboardingPreferences.didShowOnboarding.first()
            ) {
                onEvent(ChecklistCatalogEvent.UnassignedPaymentPresent)
            }
        }
        viewModelScope.launch {
            onboardingPreferences.didShowOnboarding
                .flatMapLatest { didShowOnboarding ->
                    if (didShowOnboarding) {
                        ChecklistCatalogEvent.LoadChecklistCatalog
                    } else {
                        ChecklistCatalogEvent.GoToOnboarding
                    }.let(::flowOf)
                }
                .onEach(::onEvent)
                .collect()
        }
    }

    override fun buildMviFlow(eventFlow: Flow<ChecklistCatalogEvent>): Flow<Pair<ChecklistCatalogState?, ChecklistCatalogEffect?>> {
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

    private fun Flow<ChecklistCatalogEvent>.handleLoadCatalog(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.LoadChecklistCatalog>()
            .flatMapLatest {
                getAllTemplatesUseCase.getAllTemplates()
                    .map {
                        ChecklistCatalogLoadingState.Success(
                            templates = it,
                            canAddTemplate = canAddTemplate(getCurrentUserUseCase.getCurrentUser(), it)
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

    private fun Flow<ChecklistCatalogEvent>.handleRefreshCatalog(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.PulledToRefresh>()
            .flatMapLatest {
                flow {
                    emit(state.first().copy(isRefreshing = true) to null)
                    synchronizeDataUseCase.synchronizeData()
                    emit(state.first().copy(isRefreshing = false) to null)
                }
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleGoToOnboarding(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.GoToOnboarding>()
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToOnboarding
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleUnassignedPaymentPresent(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.UnassignedPaymentPresent>()
            .map {
                state.first() to ChecklistCatalogEffect.ShowUnassignedPaymentDialog
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleCreateAccountForPaymentClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.CreateAccountForPaymentClicked>()
            .mapLatest {
                state.first() to ChecklistCatalogEffect.NavigateToAccountScreen(true)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleAccountClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.AccountClicked>()
            .mapLatest {
                state.first() to ChecklistCatalogEffect.NavigateToAccountScreen(false)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleUseTemplateClicked(): Flow<Pair<ChecklistCatalogState?, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.UseTemplateClicked>()
            .onEach {
                tracker.logEvent("template_clicked")
            }
            .map { event ->
                null to ChecklistCatalogEffect.CreateAndNavigateToChecklist(basedOn = event.template.id)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleRecentChecklistClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.RecentChecklistClicked>()
            .onEach {
                tracker.logEvent("recent_checklist_clicked")
            }
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToChecklist(checklistId = it.checklistId)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleRecentChecklistInTemplateClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.RecentChecklistClickedInTemplate>()
            .onEach {
                tracker.logEvent("recent_checklist_under_template_clicked")
            }
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToChecklist(checklistId = it.checklistId)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleEditTemplateClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.EditTemplateClicked>()
            .onEach {
                tracker.logEvent("catalog_edit_template_clicked")
            }
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToTemplateEdit(templateId = it.templateId)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleNewTemplateClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.NewTemplateClicked>()
            .onEach {
                tracker.logEvent("new_template_clicked")
            }
            .withSuccessState()
            .mapLatest { (state, _) ->
                val user = getCurrentUserUseCase.getCurrentUserFlow().first()
                val effect = if (canAddTemplate(user, state.templates)) {
                    ChecklistCatalogEffect.NavigateToNewTemplate
                } else {
                    ChecklistCatalogEffect.NavigateToPaymentScreen
                }
                _state.first() to effect
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleGetProClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.GetCheckstoryProClicked>()
            .onEach {
                tracker.logEvent("catalog_get_pro_option_clicked")
            }
            .mapLatest {
                _state.first() to ChecklistCatalogEffect.NavigateToPaymentScreen
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleAboutClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.AboutClicked>()
            .onEach {
                tracker.logEvent("about_clicked")
            }
            .mapLatest {
                _state.first() to ChecklistCatalogEffect.NavigateToAboutScreen
            }
    }

    private fun canAddTemplate(user: User, list: List<Template>): Boolean {
        return list.count() < MAX_FREE_TEMPLATES || user.isPaidUser
    }

    private fun Flow<ChecklistCatalogEvent>.handleHistoryClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.TemplateHistoryClicked>()
            .onEach {
                tracker.logEvent("catalog_template_history_clicked")
            }
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToTemplateHistory(templateId = it.templateId)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleDeleteTemplateConfirmed(): Flow<Pair<ChecklistCatalogState?, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.DeleteTemplateConfirmed>()
            .onEach {
                tracker.logEvent("catalog_template_deleted")
            }
            .map {
                deleteTemplateUseCase.deleteTemplate(it.templateId)
                null to null // TODO maybe show a toast?
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleDeleteChecklistConfirmed(): Flow<Pair<ChecklistCatalogState?, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.DeleteChecklistConfirmed>()
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
