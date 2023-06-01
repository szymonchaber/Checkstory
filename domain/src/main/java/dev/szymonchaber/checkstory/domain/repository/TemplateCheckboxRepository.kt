package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox

interface TemplateCheckboxRepository {

    suspend fun deleteFromTemplate(template: Template)

    suspend fun deleteTemplateCheckbox(templateCheckbox: TemplateCheckbox)

    suspend fun deleteTemplateCheckboxes(templateCheckboxes: List<TemplateCheckbox>)
}
