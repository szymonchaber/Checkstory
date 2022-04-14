package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import kotlinx.coroutines.flow.Flow

interface TemplateCheckboxRepository {

    fun updateTemplateCheckbox(
        templateCheckbox: TemplateCheckbox,
        templateId: ChecklistTemplateId
    ): Flow<Unit>

    fun createTemplateCheckbox(templateId: ChecklistTemplateId): Flow<TemplateCheckbox>

    suspend fun deleteFromTemplate(checklistTemplate: ChecklistTemplate)

    suspend fun deleteTemplateCheckbox(templateCheckbox: TemplateCheckbox)
}
