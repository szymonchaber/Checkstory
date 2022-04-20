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
            eventFlow.handleChildItemChanged()
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
                when (event.checkbox) {
                    is EditTemplateCheckbox.Existing -> {
                        deleteTemplateCheckboxUseCase.deleteTemplateCheckbox(event.checkbox.checkbox)
                    }
                    is EditTemplateCheckbox.New -> Unit
                }
                val newLoadingState = loadingState.updateTemplate {
                    copy(items = items.minus(event.checkbox.checkbox))
                }.copy(newCheckboxes = loadingState.newCheckboxes.minus(event.checkbox.checkbox))
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemAdded>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = when (event.parent) {
                    is EditTemplateCheckbox.Existing -> {
                        loadingState.updateTemplate {
                            copy(items = loadingState.checklistTemplate.items.updateById(
                                event.parent.checkbox.id
                            ) {
                                it.copy(
                                    children = it.children.plus(
                                        TemplateCheckbox(
                                            TemplateCheckboxId(0),
                                            event.parent.checkbox.id,
                                            "New checkbox",
                                            listOf()
                                        )
                                    )
                                )
                            }
                            )
                        }
                    }
                    is EditTemplateCheckbox.New -> {
                        loadingState.copy(newCheckboxes = loadingState.newCheckboxes.updateById(
                            event.parent.checkbox.id
                        ) {
                            it.copy(
                                children = it.children.plus(
                                    TemplateCheckbox(
                                        TemplateCheckboxId(0),
                                        event.parent.checkbox.id,
                                        "New checkbox",
                                        listOf()
                                    )
                                )
                            )
                        })
                    }
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemAdded(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = when (event.checkbox) {
                    is EditTemplateCheckbox.Existing -> {
                        loadingState.updateTemplate {
                            copy(items = loadingState.checklistTemplate.items.updateById(
                                event.checkbox.checkbox.id
                            ) { it.copy(title = event.newTitle) })
                        }
                    }
                    is EditTemplateCheckbox.New -> {
                        loadingState.copy(
                            newCheckboxes = loadingState.newCheckboxes.updateById(
                                event.checkbox.checkbox.id
                            ) { it.copy(title = event.newTitle) })
                    }
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemDeleted(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemDeleted>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = when (event.checkbox) {
                    is EditTemplateCheckbox.Existing -> {
                        loadingState.updateTemplate {
                            copy(items = loadingState.checklistTemplate.items.updateById(
                                event.checkbox.checkbox.id
                            ) {
                                it.copy(children = it.children.minus(event.child))
                            })
                        }
                    }
                    is EditTemplateCheckbox.New -> {
                        loadingState.copy(
                            newCheckboxes = loadingState.newCheckboxes.updateById(
                                event.checkbox.checkbox.id
                            ) {
                                it.copy(children = it.children.minus(event.child))
                            })
                    }
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = when (event.checkbox) {
                    is EditTemplateCheckbox.Existing -> {
                        loadingState.updateTemplate {
                            copy(items = loadingState.checklistTemplate.items.updateById(
                                event.checkbox.checkbox.id
                            ) {
                                it.copy(children = it.children.map {
                                    if (it.id == event.child.id) {
                                        it.copy(title = event.newTitle)
                                    } else {
                                        it
                                    }
                                })
                            })
                        }
                    }
                    is EditTemplateCheckbox.New -> {
                        loadingState.copy(
                            newCheckboxes = loadingState.newCheckboxes.updateById(
                                event.checkbox.checkbox.id
                            ) {
                                it.copy(children = it.children.map {
                                    if (it.id == event.child.id) {
                                        it.copy(title = event.newTitle)
                                    } else {
                                        it
                                    }
                                })
                            })
                    }
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun List<TemplateCheckbox>.updateById(
        id: TemplateCheckboxId, map: (TemplateCheckbox) -> TemplateCheckbox
    ): List<TemplateCheckbox> {
        return map {
            if (it.id == id) {
                map(it)
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
            .mapLatest { (loadingState, _) ->
                val checklistTemplate = loadingState
                    .updateTemplate {
                        copy(items = items.plus(loadingState.newCheckboxes.map { it.copy(id = TemplateCheckboxId(0)) }))
                    }
                    .checklistTemplate
                updateChecklistTemplateUseCase.updateChecklistTemplate(checklistTemplate)
                null to EditTemplateEffect.CloseScreen
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
