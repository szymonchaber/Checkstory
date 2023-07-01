package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.DeepChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistSaved
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChecklistRepositoryImpl @Inject constructor(
    private val checklistDao: ChecklistDao,
    private val commandRepository: CommandRepository
) : ChecklistRepository {

    private val _checklistSavedEvents =
        MutableSharedFlow<ChecklistSaved>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val checklistSavedEvents: Flow<ChecklistSaved>
        get() = _checklistSavedEvents

    override suspend fun save(checklist: Checklist) {
        checklistDao.insert(ChecklistEntity.fromDomainChecklist(checklist))
        checklistDao.insertAll(checklist.items.map(CheckboxEntity::fromDomainTask))
        _checklistSavedEvents.tryEmit(ChecklistSaved)
    }

    override fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return flow {
            emit(getByIdOrNull(checklistId.id))
        }
            .filterNotNull()
            .take(1)
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .map {
                it.map(DeepChecklistEntity::toDomain)
            }
            .hydrated()
    }

    private suspend fun getByIdOrNull(id: UUID): Checklist? {
        return checklistDao.getByIdOrNull(id)
            ?.let {
                commandRepository.hydrate(it.toDomain())
            } ?: commandRepository.commandOnlyChecklists()
            .find { it.id.id == id }
    }

    private fun Flow<List<Checklist>>.hydrated(): Flow<List<Checklist>> =
        combine(commandRepository.getUnappliedCommandsFlow()) { templates, _ ->
            templates
                .map {
                    commandRepository.hydrate(it)
                }
                .plus(commandRepository.commandOnlyChecklists())
                .distinctBy { it.id }
                .sortedByDescending(Checklist::createdAt)
                .filterNot(Checklist::isRemoved)
        }

    suspend fun insert(checklist: Checklist): ChecklistId {
        checklistDao.insert(ChecklistEntity.fromDomainChecklist(checklist))
        checklistDao.insertAll(checklist.flattenedItems.map(CheckboxEntity::fromDomainTask))
        return checklist.id
    }

    suspend fun replaceData(with: List<Checklist>) {
        val checklists = with.map(ChecklistEntity.Companion::fromDomainChecklist)
        val flatTasks = with.flatMap(Checklist::flattenedItems).map(CheckboxEntity.Companion::fromDomainTask)
        checklistDao.replaceData(checklists, flatTasks)
    }

    override suspend fun deleteAllData() {
        checklistDao.deleteAllData()
    }

    class TaskToChildren(
        val task: CheckboxEntity,
        val children: MutableList<TaskToChildren> = mutableListOf()
    )
}
