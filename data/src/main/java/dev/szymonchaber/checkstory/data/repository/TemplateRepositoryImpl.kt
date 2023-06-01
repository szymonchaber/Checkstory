package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.ReminderDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
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
    private val templateCheckboxDao: TemplateCheckboxDao,
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
                    templateCheckboxDao.insertAll(
                        template.flattenedItems
                            .map(TemplateCheckboxEntity::fromDomainTemplateCheckbox)
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
            val checkboxesFlow = templateCheckboxDao.getAllForTemplate(entity.id)
            val checklistsFlow = checklistRepository.getBasedOn(TemplateId(entity.id))
            val remindersFlow = reminderDao.getAllForTemplate(entity.id)
            combine(checkboxesFlow, checklistsFlow, remindersFlow) { checkboxes, checklists, reminders ->
                mapTemplate(entity, checkboxes, checklists, reminders)
            }
        }
    }

    private fun mapTemplate(
        template: ChecklistTemplateEntity,
        checkboxes: List<TemplateCheckboxEntity>,
        checklists: List<Checklist>,
        reminders: List<ReminderEntity>
    ): Template {
        return with(template) {
            Template(
                TemplateId(id),
                title,
                description,
                groupToDomain(checkboxes),
                template.createdAt,
                checklists,
                reminders.map(ReminderEntity::toDomainReminder)
            )
        }
    }

    private fun groupToDomain(checkboxes: List<TemplateCheckboxEntity>): List<TemplateCheckbox> {
        return convertToNestedCheckboxes(checkboxes)
    }

    private fun convertToNestedCheckboxes(entities: List<TemplateCheckboxEntity>): List<TemplateCheckbox> {
        val entityMap = entities.associateBy { it.checkboxId }
        val checkboxes = entities.map { CheckboxToChildren(it) }
        checkboxes.forEach { checkbox ->
            val parentId = checkbox.checkbox.parentId
            if (parentId != null) {
                val parent = entityMap[parentId]
                if (parent != null) {
                    val parentCheckbox = checkboxes.firstOrNull { it.checkbox.checkboxId == parent.checkboxId }
                    if (parentCheckbox != null) {
                        parentCheckbox.children += checkbox
                    }
                }
            }
        }
        return checkboxes.filter { it.checkbox.parentId == null }
            .map(::toDomain)
    }

    private fun toDomain(checkboxToChildren: CheckboxToChildren): TemplateCheckbox {
        return checkboxToChildren.checkbox.toTemplateCheckbox(checkboxToChildren.children.map { toDomain(it) })
    }

    class CheckboxToChildren(
        val checkbox: TemplateCheckboxEntity,
        val children: MutableList<CheckboxToChildren> = mutableListOf()
    )

    override suspend fun delete(template: Template) {
        templateDao.delete(ChecklistTemplateEntity.fromDomainTemplate(template))
        deleteTemplateReminders(template)
    }

    private suspend fun deleteTemplateReminders(template: Template) {
        reminderDao.deleteAllFromTemplate(template.id.id)
    }

    suspend fun deleteCheckboxesFromTemplate(template: Template) {
        templateCheckboxDao.deleteAllFromTemplate(template.id.id)
    }

    suspend fun deleteTemplateCheckbox(templateCheckbox: TemplateCheckbox) {
        templateCheckboxDao.deleteCascading(templateCheckbox.id.id)
    }

    suspend fun deleteTemplateCheckboxes(templateCheckboxes: List<TemplateCheckbox>) {
        templateCheckboxes.forEach {
            withContext(Dispatchers.IO) {
                templateCheckboxDao.deleteCascading(it.id.id)
            }
        }
    }

    override suspend fun replaceData(with: List<Template>) {
        val templates = with.map(ChecklistTemplateEntity::fromDomainTemplate)
        val flatItems =
            with.flatMap(Template::flattenedItems).map(TemplateCheckboxEntity::fromDomainTemplateCheckbox)
        val flatReminders = with.flatMap(Template::reminders).map(ReminderEntity::fromDomainReminder)
        templateDao.replaceData(templates, flatItems, flatReminders)
    }

    override suspend fun deleteAllData() {
        templateDao.deleteAll()
    }
}
