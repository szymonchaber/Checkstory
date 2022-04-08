package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepositoryImpl @Inject constructor(
    private val dataSource: ChecklistRoomDataSource
) : ChecklistRepository {

    override fun createAndGet(basedOn: ChecklistTemplate): Flow<Checklist> {
        return flow {
            val checklist = with(basedOn) {
                Checklist(
                    ChecklistId("0"),
                    basedOn.id,
                    title,
                    description,
                    items.map {
                        Checkbox(CheckboxId(0), it.title, false)
                    },
                    ""
                )
            }

            emit(dataSource.insert(checklist))
        }
            .flatMapConcat {
                getChecklist(ChecklistId(it.toString()))
            }
    }

    override suspend fun update(checklist: Checklist) {
        dataSource.update(checklist)
    }

    override fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return dataSource.getById(checklistId.id.toLong())
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return dataSource.getAll()
    }
}
