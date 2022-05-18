package dev.szymonchaber.checkstory.checklist.template.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.usecase.DeleteChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteTemplateCheckboxUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistTemplateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val getChecklistTemplateUseCase: GetChecklistTemplateUseCase,
    private val updateChecklistTemplateUseCase: UpdateChecklistTemplateUseCase,
    private val deleteTemplateCheckboxUseCase: DeleteTemplateCheckboxUseCase,
    private val deleteChecklistTemplateUseCase: DeleteChecklistTemplateUseCase
) : BaseViewModel<
        EditTemplateEvent,
        EditTemplateState,
        EditTemplateEffect
        >(
    EditTemplateState.initial
) {

    override fun buildMviFlow(eventFlow: Flow<EditTemplateEvent>): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return merge(
            eventFlow.handleCreateChecklist(),
            eventFlow.handleEditChecklist(),
            eventFlow.handleTitleChanged(),
            eventFlow.handleDescriptionChanged(),
            eventFlow.handleAddCheckboxClicked(),
            eventFlow.handleItemRemoved(),
            eventFlow.handleItemTitleChanged(),
            eventFlow.handleSaveTemplateClicked(),
            eventFlow.handleDeleteTemplateClicked(),
            eventFlow.handleChildItemAdded(),
            eventFlow.handleChildItemDeleted(),
            eventFlow.handleChildItemChanged(),
            eventFlow.handleAddReminderClicked(),
            eventFlow.handleReminderSaved()
        )
    }

    private fun Flow<EditTemplateEvent>.handleCreateChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.CreateChecklistTemplate>()
            .map {
                val newChecklistTemplate = ChecklistTemplate(
                    ChecklistTemplateId(0),
                    "New checklist template",
                    "Checklist description",
                    listOf(
                        TemplateCheckbox(TemplateCheckboxId(0), null, "Checkbox 1", listOf()),
                        TemplateCheckbox(TemplateCheckboxId(0), null, "Checkbox 2", listOf())
                    ),
                    LocalDateTime.now(),
                    listOf()
                )
                EditTemplateState(TemplateLoadingState.Success.fromTemplate(newChecklistTemplate)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleEditChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.EditChecklistTemplate>()
            .flatMapLatest { event ->
                getChecklistTemplateUseCase.getChecklistTemplate(event.checklistTemplateId)
                    .map {
                        TemplateLoadingState.Success.fromTemplate(it)
                    }
                    .onStart<TemplateLoadingState> {
                        emit(TemplateLoadingState.Loading)
                    }
                    .map {
                        EditTemplateState(it) to null
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = loadingState.updateTemplate {
                    copy(title = event.newTitle)
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleDescriptionChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DescriptionChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = loadingState.updateTemplate {
                    copy(description = event.newDescription)
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemRemoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemRemoved>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(loadingState.minusCheckbox(event.checkbox)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemAdded(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemAdded>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(loadingState.plusChildCheckbox(event.parent, "Checkbox")) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(loadingState.changeCheckboxTitle(event.checkbox, event.newTitle)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemDeleted(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemDeleted>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(loadingState.minusChildCheckbox(event.checkbox, event.child)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(
                    loadingState.changeChildCheckboxTitle(event.checkbox, event.child, event.newTitle)
                ) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleAddCheckboxClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddCheckboxClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                val newLoadingState = loadingState.plusNewCheckbox("Checkbox")
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleSaveTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.SaveTemplateClicked>()
            .withSuccessState()
            .mapLatest { (loadingState, _) ->
                val checklistTemplate = loadingState
                    .updateTemplate {
                        copy(items = loadingState.checkboxes.map(ViewTemplateCheckbox::toDomainModel))
                    }
                    .checklistTemplate
                updateChecklistTemplateUseCase.updateChecklistTemplate(checklistTemplate)
                deleteTemplateCheckboxUseCase.deleteTemplateCheckboxes(loadingState.checkboxesToDelete)
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun Flow<EditTemplateEvent>.handleDeleteTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DeleteTemplateClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                if (loadingState.checklistTemplate.isStored) {
                    deleteChecklistTemplateUseCase.deleteChecklistTemplate(loadingState.checklistTemplate)
                }
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun Flow<EditTemplateEvent>.handleAddReminderClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddReminderClicked>()
            .withSuccessState()
            .map {
                null to EditTemplateEffect.ShowAddReminderSheet
            }
    }

    private fun Flow<EditTemplateEvent>.handleReminderSaved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ReminderSaved>()
            .withSuccessState()
            .map { (success, event) ->
                EditTemplateState(success.plusReminder(event.reminder)) to null
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<TemplateLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.templateLoadingState }
                .filterIsInstance<TemplateLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }
}
