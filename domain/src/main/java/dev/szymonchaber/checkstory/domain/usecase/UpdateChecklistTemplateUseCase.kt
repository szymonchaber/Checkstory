package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateChecklistTemplateUseCase @Inject constructor(
    private val templateRepository: ChecklistTemplateRepository
) {

    suspend fun updateChecklistTemplate(checklistTemplate: ChecklistTemplate) {
        withContext(Dispatchers.Default) {
            templateRepository.update(trimEndingWhitespaces(checklistTemplate))
        }
    }

    private fun trimEndingWhitespaces(checklistTemplate: ChecklistTemplate): ChecklistTemplate {
        return with(checklistTemplate) {
            copy(
                title = title.trimEnd(),
                description = description.trimEnd(),
                items = items.map {
                    it.trimEndTitlesRecursive()
                }
            )
        }
    }
}

private fun TemplateCheckbox.trimEndTitlesRecursive(): TemplateCheckbox {
    return copy(title = title.trimEnd(), children = children.map { it.trimEndTitlesRecursive() })
}
