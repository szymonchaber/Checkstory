package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistTemplateRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistTemplateRepositoryImpl @Inject constructor(
    private val dataSource: ChecklistTemplateRoomDataSource
) : ChecklistTemplateRepository {

    override fun getAll(): Flow<List<ChecklistTemplate>> {
        return dataSource.getAll()
    }

    override fun get(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate> {
        return dataSource.getById(checklistTemplateId.id)
    }

    override suspend fun update(checklistTemplate: ChecklistTemplate) {
        dataSource.update(checklistTemplate)
    }

    override suspend fun delete(checklistTemplate: ChecklistTemplate) {
        dataSource.delete(checklistTemplate)
    }
}
