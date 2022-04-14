package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import javax.inject.Inject

class CreateChecklistFromTemplateUseCase @Inject constructor(
    private val checklistTemplateRepository: ChecklistTemplateRepository,
    private val checklistRepository: ChecklistRepository,
) {

    fun createChecklistFromTemplate(checklistTemplateId: ChecklistTemplateId): Flow<Checklist> {
        return checklistTemplateRepository.get(checklistTemplateId)
            .flatMapConcat {
                checklistRepository.createAndGet(it)
            }
    }
}
