package dev.szymonchaber.checkstory.checklist.fill.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.CreateChecklistFromTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistToFillUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateCheckboxUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistUseCase
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FillChecklistViewModel @Inject constructor(
    private val getChecklistToFillUseCase: GetChecklistToFillUseCase,
    private val createChecklistFromTemplateUseCase: CreateChecklistFromTemplateUseCase,
    private val updateChecklistUseCase: UpdateChecklistUseCase,
    private val updateCheckboxUseCase: UpdateCheckboxUseCase,
) :
    BaseViewModel<FillChecklistEvent, FillChecklistState, FillChecklistEffect>(
        FillChecklistState.initial
    ) {

    override fun buildMviFlow(eventFlow: Flow<FillChecklistEvent>): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return merge(
            eventFlow.handleCreateChecklist(),
            eventFlow.handleLoadChecklist(),
            eventFlow.handleCheckChanged(),
            eventFlow.handleNotesChanged(),
            eventFlow.handleEditClicked(),
            eventFlow.handleSaveClicked()
        )
    }

    private fun Flow<FillChecklistEvent>.handleCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.CheckChanged>()
            .mapLatest {
                updateCheckboxUseCase.updateCheckbox(it.item.copy(isChecked = it.newCheck))
                state.first() to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleLoadChecklist(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.LoadChecklist>()
            .flatMapLatest { loadEvent ->
                getChecklistToFillUseCase.getChecklist(loadEvent.checklistId).map {
                    FillChecklistState(ChecklistLoadingState.Success(it)) to null
                }
            }
    }

    private fun Flow<FillChecklistEvent>.handleCreateChecklist(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.CreateChecklistFromTemplate>()
            .flatMapLatest { loadEvent ->
                createChecklistFromTemplateUseCase.createChecklistFromTemplate(loadEvent.checklistTemplateId)
                    .map {
                        FillChecklistState(ChecklistLoadingState.Success(it)) to null
                    }
            }
    }

    private fun Flow<FillChecklistEvent>.handleNotesChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.NotesChanged>()
            .withSuccessState()
            .map { (success, event) ->
                val updatedLoadingState = success.updateChecklist {
                    copy(notes = event.notes)
                }
                state.first().copy(checklistLoadingState = updatedLoadingState) to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleEditClicked(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.EditTemplateClicked>()
            .flatMapLatest {
                state.map { it.checklistLoadingState }
                    .filterIsInstance<ChecklistLoadingState.Success>().take(1)
            }
            .map {
                state.first() to FillChecklistEffect.NavigateToEditTemplate(it.checklist.checklistTemplateId)
            }
    }

    private fun Flow<FillChecklistEvent>.handleSaveClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.SaveChecklistClicked>()
            .withSuccessState()
            .map { (success, _) ->
                updateChecklistUseCase.updateChecklist(success.checklist)
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<ChecklistLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.checklistLoadingState }
                .filterIsInstance<ChecklistLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }
}
