package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateChecklistTemplateUseCase @Inject constructor(
    private val templateRepository: ChecklistTemplateRepository
) {

    fun updateChecklistTemplate(checklistTemplate: ChecklistTemplate): Flow<Unit> {
        return templateRepository.update(checklistTemplate)
    }
}
