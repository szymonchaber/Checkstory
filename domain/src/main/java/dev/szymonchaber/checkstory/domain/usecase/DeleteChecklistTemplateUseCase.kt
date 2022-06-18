package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import kotlinx.coroutines.*
import javax.inject.Inject

class DeleteChecklistTemplateUseCase @Inject constructor(
    private val templateRepository: ChecklistTemplateRepository,
    private val checklistRepository: ChecklistRepository,
    private val checkboxRepository: TemplateCheckboxRepository
) {

    suspend fun deleteChecklistTemplate(checklistTemplate: ChecklistTemplate) {
        CoroutineScope(Dispatchers.Default).launch {
            awaitAll(
                async {
                    checklistRepository.deleteBasedOnTemplate(checklistTemplate)
                },
                async {
                    checkboxRepository.deleteFromTemplate(checklistTemplate)
                },
                async {
                    templateRepository.delete(checklistTemplate)
                }
            )
        }
    }
}
