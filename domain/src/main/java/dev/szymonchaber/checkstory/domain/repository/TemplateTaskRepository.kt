package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask

interface TemplateTaskRepository {

    suspend fun deleteFromTemplate(template: Template)

    suspend fun deleteTemplateTask(templateTask: TemplateTask)

    suspend fun deleteTemplateTasks(templateTasks: List<TemplateTask>)
}
