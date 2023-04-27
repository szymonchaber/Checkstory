package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import javax.inject.Inject

class GetChecklistTemplateUseCase @Inject constructor(
    private val templateRepository: ChecklistTemplateRepository
) {

    suspend fun getChecklistTemplate(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate? {
        return templateRepository.get(checklistTemplateId)
    }

    suspend fun getChecklistTemplateOrNull(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate? {
        return templateRepository.getOrNull(checklistTemplateId)
    }
}
