package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.flow.Flow

interface ChecklistTemplateRepository {

    fun getAllChecklistTemplates(): Flow<List<ChecklistTemplate>>

    fun getChecklistTemplate(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate>
}
