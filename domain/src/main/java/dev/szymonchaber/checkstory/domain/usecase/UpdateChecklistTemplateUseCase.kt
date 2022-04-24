package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import javax.inject.Inject

class UpdateChecklistTemplateUseCase @Inject constructor(
    private val templateRepository: ChecklistTemplateRepository
) {

    suspend fun updateChecklistTemplate(checklistTemplate: ChecklistTemplate) {
        templateRepository.update(checklistTemplate)
    }
}
