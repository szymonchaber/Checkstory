package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import javax.inject.Inject

class GetTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {

    suspend fun getTemplate(templateId: TemplateId): Template? {
        return templateRepository.get(templateId)
    }
}
