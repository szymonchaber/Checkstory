package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetAllTemplatesUseCase @Inject constructor(
    private val checklistRepository: TemplateRepository,
    private val synchronizeDataUseCase: SynchronizeDataUseCase
) {

    fun getAllTemplates(): Flow<List<Template>> {
        return checklistRepository.getAll().onStart {
//            synchronizeDataUseCase.synchronizeData()
        }
    }
}
