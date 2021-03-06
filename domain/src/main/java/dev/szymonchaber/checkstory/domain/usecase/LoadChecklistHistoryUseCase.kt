package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadChecklistHistoryUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {

    fun loadChecklistHistory(templateId: ChecklistTemplateId): Flow<List<Checklist>> {
        return checklistRepository.getChecklists(templateId)
    }
}
