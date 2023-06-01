package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TemplateCheckboxRepositoryImpl @Inject constructor(
    private val dataSource: TemplateRepositoryImpl
) : TemplateCheckboxRepository {

    override suspend fun deleteFromTemplate(template: Template) {
        dataSource.deleteCheckboxesFromTemplate(template)
    }

    override suspend fun deleteTemplateCheckbox(templateCheckbox: TemplateCheckbox) {
        dataSource.deleteTemplateCheckbox(templateCheckbox)
    }

    override suspend fun deleteTemplateCheckboxes(templateCheckboxes: List<TemplateCheckbox>) {
        dataSource.deleteTemplateCheckboxes(templateCheckboxes)
    }
}
