package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateCheckboxRepositoryImpl @Inject constructor(
    private val dataSource: ChecklistTemplateRepositoryImpl
) : TemplateCheckboxRepository {

    override suspend fun deleteFromTemplate(checklistTemplate: ChecklistTemplate) {
        dataSource.deleteCheckboxesFromTemplate(checklistTemplate)
    }

    override suspend fun deleteTemplateCheckbox(templateCheckbox: TemplateCheckbox) {
        dataSource.deleteTemplateCheckbox(templateCheckbox)
    }

    override suspend fun deleteTemplateCheckboxes(templateCheckboxes: List<TemplateCheckbox>) {
        dataSource.deleteTemplateCheckboxes(templateCheckboxes)
    }
}
