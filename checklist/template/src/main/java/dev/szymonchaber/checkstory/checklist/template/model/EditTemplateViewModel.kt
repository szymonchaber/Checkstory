package dev.szymonchaber.checkstory.checklist.template.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.checklist.template.EditTemplateCheckbox
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.usecase.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val createChecklistTemplateUseCase: CreateChecklistTemplateUseCase,
    private val getChecklistTemplateUseCase: GetChecklistTemplateUseCase,
    private val updateChecklistTemplateUseCase: UpdateChecklistTemplateUseCase,
    private val createTemplateCheckboxUseCase: CreateTemplateCheckboxUseCase,
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
            eventFlow.handleDeleteTemplateClicked()
        )
    }

    private fun Flow<EditTemplateEvent>.handleCreateChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.CreateChecklistTemplate>()
            .flatMapLatest {
                createChecklistTemplateUseCase.createChecklistTemplate()
                    .map {
                        TemplateLoadingState.Success(it)
                    }
                    .onStart<TemplateLoadingState> {
                        emit(TemplateLoadingState.Loading)
                    }
                    .map {
                        EditTemplateState(it) to null
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleEditChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.EditChecklistTemplate>()
            .flatMapLatest { event ->
                getChecklistTemplateUseCase.getChecklistTemplate(event.checklistTemplateId)
                    .map {
                        TemplateLoadingState.Success(it)
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
                val checklistTemplate = loadingState.checklistTemplate
                val newItems = checklistTemplate.items.filterNot {
                    it == event.checkbox.checkbox
                }
                val newLoadingState = loadingState.updateTemplate {
                    copy(items = newItems)
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = when (event.checkbox) {
                    is EditTemplateCheckbox.Existing -> {
                        loadingState.updateTemplate {
                            copy(items = loadingState.checklistTemplate.items.updateById(event))
                        }
                    }
                    is EditTemplateCheckbox.New -> {
                        loadingState.copy(newCheckboxes = loadingState.newCheckboxes.updateById(event))
                    }
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun List<TemplateCheckbox>.updateById(event: EditTemplateEvent.ItemTitleChanged): List<TemplateCheckbox> {
        return map {
            if (it.id == event.checkbox.checkbox.id) {
                it.copy(title = event.newTitle)
            } else {
                it
            }
        }
    }

    private fun Flow<EditTemplateEvent>.handleAddCheckboxClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddCheckboxClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                val newLoadingState = loadingState.plusCheckbox("Checkbox")
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleSaveTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.SaveTemplateClicked>()
            .withSuccessState()
            .flatMapLatest { (loadingState, _) ->
                val checklistTemplate = loadingState
                    .updateTemplate {
                        copy(items = items.plus(loadingState.newCheckboxes.map { it.copy(id = TemplateCheckboxId(0)) }))
                    }
                    .checklistTemplate
                updateChecklistTemplateUseCase.updateChecklistTemplate(checklistTemplate)
                    .map {
                        null to EditTemplateEffect.CloseScreen
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleDeleteTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DeleteTemplateClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                deleteChecklistTemplateUseCase.deleteChecklistTemplate(loadingState.checklistTemplate)
                null to EditTemplateEffect.CloseScreen
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
