package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.repository.TemplateTaskRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TemplateTaskRepositoryImpl @Inject constructor(
    private val dataSource: TemplateRepositoryImpl
) : TemplateTaskRepository {

    override suspend fun deleteFromTemplate(template: Template) {
        dataSource.deleteCheckboxesFromTemplate(template)
    }

    override suspend fun deleteTemplateTask(templateTask: TemplateTask) {
        dataSource.deleteTemplateTask(templateTask)
    }

    override suspend fun deleteTemplateTasks(templateTasks: List<TemplateTask>) {
        dataSource.deleteTemplateTasks(templateTasks)
    }
}
