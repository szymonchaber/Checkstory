package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ChecklistRepositoryImpl @Inject constructor() : ChecklistRepository {

    override fun createAndGet(basedOn: ChecklistTemplate): Flow<Checklist> {
        return flow {
            // TODO add to database & return with assigned id
            with(basedOn) {
                Checklist(
                    ChecklistId("GeneratedId"),
                    title,
                    description,
                    items.map {
                        Checkbox(it.title, false)
                    },
                    ""
                )
            }.let {
                emit(it)
            }
        }
            .onEach {
                delay(500)
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getChecklist(checklistId: String): Flow<Checklist> {
        return flowOf(checklist)
            .onEach {
                delay(1000)
            }
            .flowOn(Dispatchers.IO)
    }
}
