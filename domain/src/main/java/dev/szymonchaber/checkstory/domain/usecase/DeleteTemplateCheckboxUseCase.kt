package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteTemplateCheckboxUseCase @Inject constructor(
    private val templateCheckboxRepository: TemplateCheckboxRepository
) {

    suspend fun deleteTemplateCheckbox(templateCheckbox: TemplateCheckbox) {
        return templateCheckboxRepository.deleteTemplateCheckbox(templateCheckbox)
    }
}
