package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.dao.ReminderDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
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
internal class ChecklistTemplateRepositoryImpl @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val templateCheckboxDao: TemplateCheckboxDao,
    private val reminderDao: ReminderDao,
    private val commandRepository: CommandRepository,
    private val checklistRepository: ChecklistRepositoryImpl
) : ChecklistTemplateRepository {

    override suspend fun get(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate? {
        return checklistTemplateDao.getByIdOrNull(checklistTemplateId.id)
            ?.let { combineIntoDomainChecklistTemplate(it) }
            ?.firstOrNull()
            ?.let {
                commandRepository.hydrate(it)
            }
            ?: commandRepository.commandOnlyTemplates().find { it.id == checklistTemplateId }
    }

    override fun getAll(): Flow<List<ChecklistTemplate>> {
        return checklistTemplateDao.getAll()
            .flatMapLatest {
                withContext(Dispatchers.Default) {
                    it.map { combineIntoDomainChecklistTemplate(it) }.toFlowOfLists()
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

    override suspend fun update(checklistTemplate: ChecklistTemplate) {
        insert(checklistTemplate)
    }

    suspend fun insert(checklistTemplate: ChecklistTemplate): UUID {
        return withContext(Dispatchers.Default) {
            val checklistTemplateId = checklistTemplate.id.id
            checklistTemplateDao.insert(
                ChecklistTemplateEntity.fromDomainChecklistTemplate(checklistTemplate)
            )
            awaitAll(
                async {
                    templateCheckboxDao.insertAll(
                        checklistTemplate.flattenedItems
                            .map(TemplateCheckboxEntity::fromDomainTemplateCheckbox)
                    )
                },
                async {
                    reminderDao.insertAll(checklistTemplate.reminders.map(ReminderEntity::fromDomainReminder))
                }
            )
            checklistTemplateId
        }
    }

    private suspend fun combineIntoDomainChecklistTemplate(entity: ChecklistTemplateEntity): Flow<ChecklistTemplate> {
        return withContext(Dispatchers.Default) {
            val checkboxesFlow = templateCheckboxDao.getAllForChecklistTemplate(entity.id)
            val checklistsFlow = checklistRepository.getBasedOn(ChecklistTemplateId(entity.id))
            val remindersFlow = reminderDao.getAllForChecklistTemplate(entity.id)
            combine(checkboxesFlow, checklistsFlow, remindersFlow) { checkboxes, checklists, reminders ->
                mapChecklistTemplate(entity, checkboxes, checklists, reminders)
            }
        }
    }

    private fun mapChecklistTemplate(
        template: ChecklistTemplateEntity,
        checkboxes: List<TemplateCheckboxEntity>,
        checklists: List<Checklist>,
        reminders: List<ReminderEntity>
    ): ChecklistTemplate {
        return with(template) {
            ChecklistTemplate(
                ChecklistTemplateId(id),
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

    override suspend fun delete(checklistTemplate: ChecklistTemplate) {
        checklistTemplateDao.delete(ChecklistTemplateEntity.fromDomainChecklistTemplate(checklistTemplate))
        deleteTemplateReminders(checklistTemplate)
    }

    private suspend fun deleteTemplateReminders(checklistTemplate: ChecklistTemplate) {
        reminderDao.deleteAllFromTemplate(checklistTemplate.id.id)
    }

    suspend fun deleteCheckboxesFromTemplate(checklistTemplate: ChecklistTemplate) {
        templateCheckboxDao.deleteAllFromTemplate(checklistTemplate.id.id)
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

    override suspend fun replaceData(with: List<ChecklistTemplate>) {
        val templates = with.map(ChecklistTemplateEntity::fromDomainChecklistTemplate)
        val flatItems =
            with.flatMap(ChecklistTemplate::flattenedItems).map(TemplateCheckboxEntity::fromDomainTemplateCheckbox)
        val flatReminders = with.flatMap(ChecklistTemplate::reminders).map(ReminderEntity::fromDomainReminder)
        checklistTemplateDao.replaceData(templates, flatItems, flatReminders)
    }

    override suspend fun deleteAllData() {
        checklistTemplateDao.deleteAll()
    }
}
