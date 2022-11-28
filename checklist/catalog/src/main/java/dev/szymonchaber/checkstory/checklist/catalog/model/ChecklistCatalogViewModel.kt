package dev.szymonchaber.checkstory.checklist.catalog.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplatesUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetRecentChecklistsUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class ChecklistCatalogViewModel @Inject constructor(
    private val getChecklistTemplatesUseCase: GetChecklistTemplatesUseCase,
    private val getRecentChecklistsUseCase: GetRecentChecklistsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val tracker: Tracker
) : BaseViewModel<
        ChecklistCatalogEvent,
        ChecklistCatalogState,
        ChecklistCatalogEffect
        >(
    ChecklistCatalogState.initial
) {

    init {
        onEvent(ChecklistCatalogEvent.LoadChecklistCatalog)
    }

    override fun buildMviFlow(eventFlow: Flow<ChecklistCatalogEvent>): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return merge(
            eventFlow.handleLoadChecklist(),
            eventFlow.handleTemplateClicked(),
            eventFlow.handleRecentChecklistClicked(),
            eventFlow.handleRecentChecklistInTemplateClicked(),
            eventFlow.handleNewTemplateClicked(),
            eventFlow.handleEditTemplateClicked(),
            eventFlow.handleHistoryClicked(),
            eventFlow.handleGetProClicked(),
            eventFlow.handleAboutClicked()
        )
    }

    private fun Flow<ChecklistCatalogEvent>.handleLoadChecklist(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.LoadChecklistCatalog>()
            .flatMapLatest {
                val templatesLoading = getChecklistTemplatesUseCase.getChecklistTemplates()
                    .map {
                        ChecklistCatalogLoadingState.Success(it)
                    }.onStart<ChecklistCatalogLoadingState> {
                        emit(ChecklistCatalogLoadingState.Loading)
                    }
                val recentChecklistsLoading = getRecentChecklistsUseCase.getRecentChecklists()
                    .map {
                        RecentChecklistsLoadingState.Success(it)
                    }.onStart<RecentChecklistsLoadingState> {
                        emit(RecentChecklistsLoadingState.Loading)
                    }

                templatesLoading.combine(recentChecklistsLoading) { templates, checklists ->
                    state.first()
                        .copy(templatesLoadingState = templates, recentChecklistsLoadingState = checklists) to null
                }
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleTemplateClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.NewChecklistFromTemplateClicked>()
            .onEach {
                tracker.logEvent("template_clicked")
            }
            .withSuccessState()
            .mapLatest { (_, event) ->
                val user = getUserUseCase.getUser().first()
                val effect = if (canAddChecklistToTemplate(user, event.template)) {
                    ChecklistCatalogEffect.CreateAndNavigateToChecklist(basedOn = event.template.id)
                } else {
                    ChecklistCatalogEffect.NavigateToPaymentScreen()
                }
                _state.first() to effect
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
                val user = getUserUseCase.getUser().first()
                val effect = if (canAddTemplate(user, state.checklistTemplates)) {
                    ChecklistCatalogEffect.NavigateToNewTemplate()
                } else {
                    ChecklistCatalogEffect.NavigateToPaymentScreen()
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
                _state.first() to ChecklistCatalogEffect.NavigateToPaymentScreen()
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleAboutClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.AboutClicked>()
            .onEach {
                tracker.logEvent("about_clicked")
            }
            .mapLatest {
                _state.first() to ChecklistCatalogEffect.NavigateToAboutScreen()
            }
    }

    private fun canAddTemplate(user: User, list: List<ChecklistTemplate>): Boolean {
        return list.count() < MAX_FREE_CHECKLIST_TEMPLATES || user.isPaidUser
    }

    private fun canAddChecklistToTemplate(user: User, template: ChecklistTemplate): Boolean {
        return template.checklists.count() < MAX_FREE_CHECKLISTS || user.isPaidUser
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

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<ChecklistCatalogLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.templatesLoadingState }
                .filterIsInstance<ChecklistCatalogLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }

    companion object {

        private const val MAX_FREE_CHECKLIST_TEMPLATES = 1
        private const val MAX_FREE_CHECKLISTS = 1
    }
}
