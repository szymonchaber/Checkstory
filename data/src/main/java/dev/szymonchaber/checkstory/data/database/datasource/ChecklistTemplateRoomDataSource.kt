package dev.szymonchaber.checkstory.data.database.datasource

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
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class ChecklistTemplateRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val templateCheckboxDao: TemplateCheckboxDao,
    private val reminderDao: ReminderDao,
    private val checklistRoomDataSource: ChecklistRoomDataSource
) {

    fun getById(id: UUID): Flow<ChecklistTemplate> {
        return checklistTemplateDao.getById(id)
            .filterNotNull()
            .flatMapLatest(::combineIntoDomainChecklistTemplate)
            .take(1)
    }

    suspend fun getByIdOrNull(id: UUID): ChecklistTemplate? {
        return checklistTemplateDao.getByIdOrNull(id)?.let { combineIntoDomainChecklistTemplate(it) }?.firstOrNull()
    }

    fun getAll(): Flow<List<ChecklistTemplate>> {
        return checklistTemplateDao.getAll()
            .flatMapLatest {
                withContext(Dispatchers.Default) {
                    it.map { combineIntoDomainChecklistTemplate(it) }.toFlowOfLists()
                }
            }
    }

    suspend fun update(checklistTemplate: ChecklistTemplate): UUID {
        return insert(checklistTemplate)
    }

    suspend fun updateAll(templates: List<ChecklistTemplate>) {
        withContext(Dispatchers.Default) {
            awaitAll(
                *templates.map {
                    async {
                        insert(it)
                    }
                }.toTypedArray()
            )
        }
    }

    suspend fun insert(checklistTemplate: ChecklistTemplate): UUID {
        return withContext(Dispatchers.Default) {
            val checklistTemplateId = checklistTemplate.id.id
            checklistTemplateDao.insert(
                ChecklistTemplateEntity.fromDomainChecklistTemplate(checklistTemplate)
            )
            awaitAll(
                async {
                    insertTemplateCheckboxes(checklistTemplate.items, checklistTemplateId)
                },
                async {
                    insertReminders(checklistTemplate.reminders, checklistTemplateId)
                }
            )
            checklistTemplateId
        }
    }

    private suspend fun insertReminders(reminders: List<Reminder>, checklistTemplateId: UUID) {
        withContext(Dispatchers.Default) {
            launch {
                val reminderEntities = reminders.map {
                    ReminderEntity.fromDomainReminder(it, checklistTemplateId)
                }
                reminderDao.insertAll(*reminderEntities.toTypedArray())
            }
        }
    }

    private suspend fun insertTemplateCheckboxes(checkboxes: List<TemplateCheckbox>, checklistTemplateId: UUID) {
        checkboxes.forEach {
            withContext(Dispatchers.Default) {
                launch {
                    insertCheckboxRecursive(it, checklistTemplateId, null)
                }
            }
        }
    }

    private suspend fun insertCheckboxRecursive(
        templateCheckbox: TemplateCheckbox,
        checklistTemplateId: UUID,
        parentId: TemplateCheckboxId?
    ) {
        templateCheckboxDao.insert(
            TemplateCheckboxEntity.fromDomainTemplateCheckbox(
                templateCheckbox,
                checklistTemplateId
            ).copy(parentId = parentId?.id)
        )
        templateCheckbox.children.forEach { child ->
            insertCheckboxRecursive(child, checklistTemplateId, templateCheckbox.id)
        }
    }

    private suspend fun combineIntoDomainChecklistTemplate(entity: ChecklistTemplateEntity): Flow<ChecklistTemplate> {
        return withContext(Dispatchers.Default) {
            val checkboxesFlow = templateCheckboxDao.getAllForChecklistTemplate(entity.id)
            val checklistsFlow = checklistRoomDataSource.getBasedOn(ChecklistTemplateId(entity.id))
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

    private fun TemplateCheckboxEntity.toTemplateCheckbox(children: List<TemplateCheckbox> = emptyList()): TemplateCheckbox {
        return TemplateCheckbox(
            id = TemplateCheckboxId(checkboxId),
            parentId = parentId?.let { TemplateCheckboxId(it) },
            title = checkboxTitle,
            children = children,
            sortPosition = sortPosition
        )
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

    suspend fun delete(checklistTemplate: ChecklistTemplate) {
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
}
