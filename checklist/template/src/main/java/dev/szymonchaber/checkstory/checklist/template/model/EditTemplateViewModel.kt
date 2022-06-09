package dev.szymonchaber.checkstory.checklist.template.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.usecase.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val getChecklistTemplateUseCase: GetChecklistTemplateUseCase,
    private val updateChecklistTemplateUseCase: UpdateChecklistTemplateUseCase,
    private val deleteTemplateCheckboxUseCase: DeleteTemplateCheckboxUseCase,
    private val deleteChecklistTemplateUseCase: DeleteChecklistTemplateUseCase,
    private val deleteRemindersUseCase: DeleteRemindersUseCase
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
            eventFlow.handleReminderClicked(),
            eventFlow.handleReminderSaved(),
            eventFlow.handleReminderDeleted()
        )
    }

    private fun Flow<EditTemplateEvent>.handleCreateChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.CreateChecklistTemplate>()
            .map {
                val newChecklistTemplate = ChecklistTemplate(
                    ChecklistTemplateId(0),
                    "",
                    "",
                    listOf(
                        TemplateCheckbox(TemplateCheckboxId(0), null, "", listOf()),
                    ),
                    LocalDateTime.now(),
                    listOf(),
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
                EditTemplateState(loadingState.plusChildCheckbox(event.parent, "")) to null
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
                val newLoadingState = loadingState.plusNewCheckbox("")
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleSaveTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.SaveTemplateClicked>()
            .withSuccessState()
            .mapLatest { (loadingState, _) ->
                val checklistTemplate = loadingState
                    .updateTemplate {
                        copy(
                            title = title.trimEnd(),
                            description = description.trim(),
                            items = loadingState.checkboxes.map(ViewTemplateCheckbox::toDomainModel)
                                .map { it.copy(title = it.title.trimEnd()) })
                    }
                    .checklistTemplate
                updateChecklistTemplateUseCase.updateChecklistTemplate(checklistTemplate)
                deleteTemplateCheckboxUseCase.deleteTemplateCheckboxes(loadingState.checkboxesToDelete)
                deleteRemindersUseCase.deleteReminders(loadingState.remindersToDelete)
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
                null to EditTemplateEffect.ShowAddReminderSheet()
            }
    }

    private fun Flow<EditTemplateEvent>.handleReminderClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ReminderClicked>()
            .withSuccessState()
            .map { (_, event) ->
                null to EditTemplateEffect.ShowEditReminderSheet(event.reminder)
            }
    }

    private fun Flow<EditTemplateEvent>.handleReminderSaved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ReminderSaved>()
            .withSuccessState()
            .map { (success, event) ->
                EditTemplateState(success.plusReminder(event.reminder)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleReminderDeleted(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DeleteReminderClicked>()
            .withSuccessState()
            .map { (success, event) ->
                EditTemplateState(success.minusReminder(event.reminder)) to null
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
