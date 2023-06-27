package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import javax.inject.Inject

class LoadChecklistHistoryUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {

    suspend fun loadChecklistHistory(templateId: TemplateId): List<Checklist>? {
        return templateRepository.get(templateId)?.checklists
    }
}
