package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.ReminderDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateTaskDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val templateTaskDao: TemplateTaskDao,
    private val reminderDao: ReminderDao,
    private val commandRepository: CommandRepository,
    private val checklistRepository: ChecklistRepositoryImpl
) : TemplateRepository {

    override suspend fun get(templateId: TemplateId): Template? {
        return templateDao.getByIdOrNull(templateId.id)
            ?.let { combineIntoDomainTemplate(it) }
            ?.firstOrNull()
            ?.let {
                commandRepository.hydrate(it)
            }
            ?: commandRepository.commandOnlyTemplates().find { it.id == templateId }
    }

    override fun getAll(): Flow<List<Template>> {
        return templateDao.getAll()
            .flatMapLatest {
                withContext(Dispatchers.Default) {
                    it.map { combineIntoDomainTemplate(it) }.toFlowOfLists()
                }
            }
            .combine(commandRepository.getUnappliedCommandsFlow()) { templates, _ ->
                templates.map {
                    commandRepository.hydrate(it)
                }
                    .plus(commandRepository.commandOnlyTemplates())
                    .sortedBy { it.createdAt }
                    .filterNot { it.isRemoved }
            }
    }

    override suspend fun update(template: Template) {
        insert(template)
    }

    suspend fun insert(template: Template): UUID {
        return withContext(Dispatchers.Default) {
            val templateId = template.id.id
            templateDao.insert(
                ChecklistTemplateEntity.fromDomainTemplate(template)
            )
            awaitAll(
                async {
                    templateTaskDao.insertAll(
                        template.flattenedTasks
                            .map(TemplateCheckboxEntity::fromDomainTemplateTask)
                    )
                },
                async {
                    reminderDao.insertAll(template.reminders.map(ReminderEntity::fromDomainReminder))
                }
            )
            templateId
        }
    }

    private suspend fun combineIntoDomainTemplate(entity: ChecklistTemplateEntity): Flow<Template> {
        return withContext(Dispatchers.Default) {
            val tasksFlow = templateTaskDao.getAllForTemplate(entity.id)
            val checklistsFlow = checklistRepository.getBasedOn(TemplateId(entity.id))
            val remindersFlow = reminderDao.getAllForTemplate(entity.id)
            combine(tasksFlow, checklistsFlow, remindersFlow) { tasks, checklists, reminders ->
                mapTemplate(entity, tasks, checklists, reminders)
            }
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
                groupToDomain(tasks),
                template.createdAt,
                checklists,
                reminders.map(ReminderEntity::toDomainReminder)
            )
        }
    }

    private fun groupToDomain(tasks: List<TemplateCheckboxEntity>): List<TemplateTask> {
        return convertToNestedTasks(tasks)
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
            .map(::toDomain)
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

    suspend fun deleteTasksFromTemplate(template: Template) {
        templateTaskDao.deleteAllFromTemplate(template.id.id)
    }

    suspend fun deleteTemplateTask(templateTask: TemplateTask) {
        templateTaskDao.deleteCascading(templateTask.id.id)
    }

    suspend fun deleteTemplateTasks(templateTasks: List<TemplateTask>) {
        templateTasks.forEach {
            withContext(Dispatchers.IO) {
                templateTaskDao.deleteCascading(it.id.id)
            }
        }
    }

    override suspend fun replaceData(with: List<Template>) {
        val templates = with.map(ChecklistTemplateEntity::fromDomainTemplate)
        val flatItems =
            with.flatMap(Template::flattenedTasks).map(TemplateCheckboxEntity::fromDomainTemplateTask)
        val flatReminders = with.flatMap(Template::reminders).map(ReminderEntity::fromDomainReminder)
        templateDao.replaceData(templates, flatItems, flatReminders)
    }

    override suspend fun deleteAllData() {
        templateDao.deleteAll()
    }
}
