package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.DeepChecklistEntity
import dev.szymonchaber.checkstory.data.database.dao.DeepTemplateEntity
import dev.szymonchaber.checkstory.data.database.dao.ReminderDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val reminderDao: ReminderDao
) : TemplateRepository {

    override suspend fun get(templateId: TemplateId): Template? {
        return templateDao.getByIdOrNull(templateId.id)
            ?.let { toDomain(it) }
    }

    override fun getAll(): Flow<List<Template>> {
        return templateDao.getAllDeep()
            .mapLatest {
                withContext(Dispatchers.Default) {
                    it.map { toDomain(it) }
                        .sortedBy { it.createdAt }
                        .filterNot { it.isRemoved }
                }
            }
    }

    override suspend fun save(template: Template) {
        withContext(Dispatchers.Default) {
            templateDao.insert(
                ChecklistTemplateEntity.fromDomainTemplate(template),
                template.flattenedTasks
                    .map(TemplateCheckboxEntity::fromDomainTemplateTask),
                template.reminders.map(ReminderEntity::fromDomainReminder)
            )
        }
    }

    private suspend fun toDomain(deepTemplate: DeepTemplateEntity): Template {
        return withContext(Dispatchers.Default) {
            val (entity, reminders, tasks, checklists) = deepTemplate
            val checklistsIncludingCommandOnly = checklists
                .map(DeepChecklistEntity::toDomain)
                .distinctBy { it.id }
                .sortedByDescending(Checklist::createdAt)
                .filterNot(Checklist::isRemoved)
                .filter { it.templateId.id == entity.id }
            mapTemplate(entity, tasks, checklistsIncludingCommandOnly, reminders)
        }
    }

    private fun mapTemplate(
        template: ChecklistTemplateEntity,
        tasks: List<TemplateCheckboxEntity>,
        checklists: List<Checklist>,
        reminders: List<ReminderEntity>
    ): Template {
        return with(template) {
            Template(
                TemplateId(id),
                title,
                description,
                convertToNestedTasks(tasks.sortedBy { it.sortPosition }),
                template.createdAt,
                template.updatedAt,
                checklists,
                reminders.map(ReminderEntity::toDomainReminder),
                isRemoved
            )
        }
    }

    private fun convertToNestedTasks(entities: List<TemplateCheckboxEntity>): List<TemplateTask> {
        val entityMap = entities.associateBy { it.checkboxId }
        val tasks = entities.map { TaskToChildren(it) }
        tasks.forEach { task ->
            val parentId = task.task.parentId
            if (parentId != null) {
                val parent = entityMap[parentId]
                if (parent != null) {
                    val parentTask = tasks.firstOrNull { it.task.checkboxId == parent.checkboxId }
                    if (parentTask != null) {
                        parentTask.children += task
                    }
                }
            }
        }
        return tasks.filter { it.task.parentId == null }
            .map(this::toDomain)
    }

    private fun toDomain(taskToChildren: TaskToChildren): TemplateTask {
        return taskToChildren.task.toTemplateTask(taskToChildren.children.map { toDomain(it) })
    }

    class TaskToChildren(
        val task: TemplateCheckboxEntity,
        val children: MutableList<TaskToChildren> = mutableListOf()
    )

    override suspend fun delete(template: Template) {
        templateDao.delete(ChecklistTemplateEntity.fromDomainTemplate(template))
        deleteTemplateReminders(template)
    }

    private suspend fun deleteTemplateReminders(template: Template) {
        reminderDao.deleteAllFromTemplate(template.id.id)
    }

    override suspend fun deleteAllData() {
        templateDao.deleteAll()
    }
}
