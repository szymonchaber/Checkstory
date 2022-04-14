package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChecklistTemplatesUseCase @Inject constructor(
    private val checklistRepository: ChecklistTemplateRepository
) {

    fun getChecklistTemplates(): Flow<List<ChecklistTemplate>> {
        return checklistRepository.getAll()
    }
}
