package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepositoryImpl @Inject constructor() : ChecklistRepository {

    private val checklistsFlow = MutableStateFlow(
        mapOf(
            ChecklistId("fakeId") to Checklist(
                ChecklistId("fakeId"),
                ChecklistTemplateRepositoryImpl.templates[0].id,
                "Cleaning something",
                "It's good to do",
                listOf(
                    Checkbox(CheckboxId(1), "Start", true),
                    Checkbox(CheckboxId(2), "Continue", true),
                    Checkbox(CheckboxId(3), "Finish", false),
                ),
                "It was a good session"
            ),
            ChecklistId("fakeId2") to Checklist(
                ChecklistId("fakeId2"),
                ChecklistTemplateRepositoryImpl.templates[1].id,
                "Cleaning the office",
                "The place to be",
                listOf(
                    Checkbox(CheckboxId(1), "Start", true),
                    Checkbox(CheckboxId(2), "Continue", true),
                    Checkbox(CheckboxId(3), "Finish", false),
                ),
                "It was a really good session"
            )
        )
    )

    override fun createAndGet(basedOn: ChecklistTemplate): Flow<Checklist> {
        return flow {
            var checkboxIndex = 1L
            with(basedOn) {
                Checklist(
                    ChecklistId(UUID.randomUUID().toString()),
                    basedOn.id,
                    title,
                    description,
                    items.map {
                        Checkbox(CheckboxId(checkboxIndex++), it.title, false)
                    },
                    ""
                )
            }.let {
                checklistsFlow.update { map ->
                    map.plus(it.id to it)
                }
                emit(it)
            }
        }
            .flowOn(Dispatchers.IO)
    }

    override fun update(checklist: Checklist): Flow<Unit> {
        return checklistsFlow.take(1)
            .onEach {
                checklistsFlow.update { map ->
                    map.plus(checklist.id to checklist)
                }
            }.map { }
    }

    override fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return checklistsFlow
            .take(1) // TODO get rid of this
            .map {
                it.getValue(checklistId)
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return checklistsFlow
            .map {
                it.values.toList()
            }
            .flowOn(Dispatchers.IO)
    }
}
