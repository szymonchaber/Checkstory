package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChecklistTemplateUseCase @Inject constructor(
    private val templateRepository: ChecklistTemplateRepository
) {

    fun getChecklistTemplate(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate> {
        return templateRepository.get(checklistTemplateId)
    }

    suspend fun getChecklistTemplateOrNull(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate? {
        return templateRepository.getOrNull(checklistTemplateId)
    }
}
