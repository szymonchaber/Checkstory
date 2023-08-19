package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTemplatesUseCase @Inject constructor(
    private val checklistRepository: TemplateRepository
) {

    fun getAllTemplates(): Flow<List<Template>> {
        return checklistRepository.getAll()
    }
}
