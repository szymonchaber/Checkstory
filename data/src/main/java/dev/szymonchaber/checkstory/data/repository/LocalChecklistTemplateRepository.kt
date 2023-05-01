package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistTemplateRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalChecklistTemplateRepository @Inject constructor(
    private val dataSource: ChecklistTemplateRoomDataSource
) : ChecklistTemplateRepository {

    override fun getAll(): Flow<List<ChecklistTemplate>> {
        return dataSource.getAll()
    }

    override suspend fun get(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate? {
        return dataSource.getByIdOrNull(checklistTemplateId.id)
    }

    override suspend fun helloWorld(token: String): String {
        return "local repo"
    }

    override suspend fun getOrNull(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate? {
        return dataSource.getByIdOrNull(checklistTemplateId.id)
    }

    override suspend fun update(checklistTemplate: ChecklistTemplate) {
        dataSource.update(checklistTemplate)
    }

    override suspend fun delete(checklistTemplate: ChecklistTemplate) {
        dataSource.delete(checklistTemplate)
    }

    suspend fun updateAll(checklistTemplatesWithRemoteId: List<ChecklistTemplate>) {
        dataSource.updateAll(checklistTemplatesWithRemoteId)
    }

    suspend fun removeAll() {
        dataSource.deleteAll()
    }
}
