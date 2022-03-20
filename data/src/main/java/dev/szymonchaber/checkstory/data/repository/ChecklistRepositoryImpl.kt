package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepositoryImpl @Inject constructor() : ChecklistRepository {

    private val checklistsFlow = MutableStateFlow(
        mapOf(
            ChecklistId("fakeId") to Checklist(
                ChecklistId("fakeId"),
                "Cleaning something",
                "It's good to do",
                listOf(
                    Checkbox("Start", true),
                    Checkbox("Continue", true),
                    Checkbox("Finish", false),
                ),
                "It was a good session"
            ),
            ChecklistId("fakeId2") to Checklist(
                ChecklistId("fakeId2"),
                "Cleaning the office",
                "The place to be",
                listOf(
                    Checkbox("Start", true),
                    Checkbox("Continue", true),
                    Checkbox("Finish", false),
                ),
                "It was a really good session"
            )
        )
    )

    override fun createAndGet(basedOn: ChecklistTemplate): Flow<Checklist> {
        return flow {
            with(basedOn) {
                Checklist(
                    ChecklistId(UUID.randomUUID().toString()),
                    title,
                    description,
                    items.map {
                        Checkbox(it.title, false)
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
            .onEach {
                delay(500)
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
            .onEach {
                delay(1000)
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return checklistsFlow
            .map {
                it.values.toList()
            }
            .onEach {
                delay(1000)
            }
            .flowOn(Dispatchers.IO)
    }
}
