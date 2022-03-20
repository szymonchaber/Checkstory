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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Singleton
class ChecklistRepositoryImpl @Inject constructor() : ChecklistRepository {

    private val checklists = mutableMapOf(
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

    private val checklistsFlow = MutableStateFlow(checklists)

    fun <T> tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO, block: (Int) -> T) = flow {
        delay(initialDelay)
        var loop = 1
        while (true) {
            emit(block(loop++))
            delay(period)
        }
    }

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
                checklists[it.id] = it
                checklistsFlow.emit(checklists)
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
                it.replace(checklist.id, checklist)
                checklistsFlow.emit(it)
            }.map { }
    }

    override fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return flowOf(checklists.getValue(checklistId))
            .onEach {
                delay(1000)
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return tickerFlow(period = 2.seconds) { // TODO This is bad
            checklists.values.toList()
        }
        return checklistsFlow.map {
            it.values.toList()
        }
            .onEach {
//                delay(1000)
            }
            .flowOn(Dispatchers.IO)
    }
}
