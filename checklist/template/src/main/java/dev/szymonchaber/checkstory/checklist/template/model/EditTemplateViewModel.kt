package dev.szymonchaber.checkstory.checklist.template.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.usecase.CreateChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistTemplateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val createChecklistTemplateUseCase: CreateChecklistTemplateUseCase,
    private val updateChecklistTemplateUseCase: UpdateChecklistTemplateUseCase
) : BaseViewModel<
        EditTemplateEvent,
        EditTemplateState,
        EditTemplateEffect
        >(
    EditTemplateState.initial
) {

    init {
        onEvent(EditTemplateEvent.CreateChecklistTemplate)
    }

    override fun buildMviFlow(eventFlow: Flow<EditTemplateEvent>): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return merge(
            eventFlow.handleCreateChecklist(),
            eventFlow.handleTitleChanged(),
            eventFlow.handleDescriptionChanged(),
            eventFlow.handleAddCheckboxClicked(),
            eventFlow.handleItemRemoved(),
            eventFlow.handleItemTitleChanged()
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

    private fun Flow<EditTemplateEvent>.handleTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TitleChanged>()
            .withSuccessState()
            .flatMapLatest { (loadingState, event) ->
                updateChecklistTemplateUseCase.updateChecklistTemplate(loadingState.checklistTemplate.copy(title = event.newTitle))
                    .map {
                        null to null
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleDescriptionChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DescriptionChanged>()
            .withSuccessState()
            .flatMapLatest { (loadingState, event) ->
                updateChecklistTemplateUseCase.updateChecklistTemplate(loadingState.checklistTemplate.copy(description = event.newDescription))
                    .map {
                        null to null
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemRemoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemRemoved>()
            .withSuccessState()
            .flatMapLatest { (loadingState, event) ->
                val checklistTemplate = loadingState.checklistTemplate
                val newItems = checklistTemplate.items.filterNot {
                    it == event.templateCheckbox
                }
                updateChecklistTemplateUseCase.updateChecklistTemplate(checklistTemplate.copy(items = newItems))
                    .map {
                        null to null
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemTitleChanged>()
            .withSuccessState()
            .flatMapLatest { (loadingState, event) ->
                val checklistTemplate = loadingState.checklistTemplate
                val newItems = checklistTemplate.items.map {
                    if (it == event.checkbox) {
                        it.copy(title = event.newTitle)
                    } else {
                        it
                    }
                }
                updateChecklistTemplateUseCase.updateChecklistTemplate(loadingState.checklistTemplate.copy(items = newItems))
                    .map {
                        null to null
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleAddCheckboxClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddCheckboxClicked>()
            .withSuccessState()
            .flatMapLatest { (loadingState, _) ->
                val checklistTemplate = loadingState.checklistTemplate
                val newItems = checklistTemplate.items.plus(TemplateCheckbox("New checkbox"))
                updateChecklistTemplateUseCase.updateChecklistTemplate(loadingState.checklistTemplate.copy(items = newItems))
                    .map {
                        null to null
                    }
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
