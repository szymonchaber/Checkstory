package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.flow.Flow

interface ChecklistTemplateRepository {

    fun getAll(): Flow<List<ChecklistTemplate>>

    fun get(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate>

    suspend fun getOrNull(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate?

    suspend fun update(checklistTemplate: ChecklistTemplate)

    suspend fun delete(checklistTemplate: ChecklistTemplate)
}
