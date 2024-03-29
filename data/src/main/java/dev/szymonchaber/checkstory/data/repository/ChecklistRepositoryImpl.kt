package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.DeepChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistSaved
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChecklistRepositoryImpl @Inject constructor(
    private val checklistDao: ChecklistDao
) : ChecklistRepository {

    private val _checklistSavedEvents =
        MutableSharedFlow<ChecklistSaved>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val checklistSavedEvents: Flow<ChecklistSaved>
        get() = _checklistSavedEvents

    override fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return flow {
            emit(get(checklistId))
        }
            .filterNotNull()
            .take(1)
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .map {
                withContext(Dispatchers.Default) {
                    it.map(DeepChecklistEntity::toDomain)
                        .sortedByDescending(Checklist::createdAt)
                        .filterNot(Checklist::isRemoved)
                }
            }
    }

    override suspend fun get(checklistId: ChecklistId): Checklist? {
        return checklistDao.getByIdOrNull(checklistId.id)?.toDomain()
    }

    override suspend fun save(checklist: Checklist) {
        checklistDao.insert(
            ChecklistEntity.fromDomainChecklist(checklist),
            checklist.flattenedItems.map(CheckboxEntity::fromDomainTask)
        )
        _checklistSavedEvents.tryEmit(ChecklistSaved)
    }

    override suspend fun deleteAllData() {
        checklistDao.deleteAllData()
    }

    class TaskToChildren(
        val task: CheckboxEntity,
        val children: MutableList<TaskToChildren> = mutableListOf()
    )
}
