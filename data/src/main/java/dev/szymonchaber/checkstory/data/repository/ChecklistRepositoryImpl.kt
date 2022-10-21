package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistSaved
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepositoryImpl @Inject constructor(
    private val dataSource: ChecklistRoomDataSource
) : ChecklistRepository {

    private val _checklistSavedEvents = MutableSharedFlow<ChecklistSaved>()

    override val checklistSavedEvents: Flow<ChecklistSaved>
        get() = _checklistSavedEvents

    override suspend fun save(checklist: Checklist) {
        dataSource.insert(checklist)
        _checklistSavedEvents.tryEmit(ChecklistSaved)
    }

    override fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return dataSource.getById(checklistId.id)
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return dataSource.getAll()
    }

    override fun getChecklists(basedOn: ChecklistTemplateId): Flow<List<Checklist>> {
        return dataSource.getBasedOn(basedOn)
    }

    override suspend fun delete(checklist: Checklist) {
        dataSource.delete(checklist)
    }

    override suspend fun deleteBasedOnTemplate(checklistTemplate: ChecklistTemplate) {
        dataSource.getBasedOn(checklistTemplate.id)
            .first()
            .forEach {
                withContext(Dispatchers.IO) {
                    dataSource.delete(it)
                }
            }
    }
}
