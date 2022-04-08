package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateTemplateCheckboxUseCase @Inject constructor(
    private val templateCheckboxRepository: TemplateCheckboxRepository
) {

    fun createChecklistTemplate(templateId: ChecklistTemplateId): Flow<TemplateCheckbox> {
        return templateCheckboxRepository.createTemplateCheckbox(templateId)
    }
}
