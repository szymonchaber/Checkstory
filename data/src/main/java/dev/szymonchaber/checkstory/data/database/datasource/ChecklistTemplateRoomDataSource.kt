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
import javax.inject.Inject

class ChecklistTemplateRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val templateCheckboxDao: TemplateCheckboxDao,
    private val reminderDao: ReminderDao,
    private val checklistRoomDataSource: ChecklistRoomDataSource
) {

    fun getById(id: Long): Flow<ChecklistTemplate> {
        return checklistTemplateDao.getById(id)
            .filterNotNull()
            .flatMapLatest(::combineIntoDomainChecklistTemplate)
            .take(1)
    }

    suspend fun getByIdOrNull(id: Long): ChecklistTemplate? {
        return checklistTemplateDao.getByIdOrNull(id)?.let { combineIntoDomainChecklistTemplate(it) }?.firstOrNull()
    }

    fun getAll(): Flow<List<ChecklistTemplate>> {
        return checklistTemplateDao.getAll()
            .flatMapLatest {
                it.map(::combineIntoDomainChecklistTemplate).toFlowOfLists()
            }
    }

    suspend fun update(checklistTemplate: ChecklistTemplate): Long {
        return insert(checklistTemplate)
    }

    suspend fun insert(checklistTemplate: ChecklistTemplate): Long {
        return withContext(Dispatchers.Default) {
            val checklistTemplateId = checklistTemplateDao.insert(
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

    private suspend fun insertReminders(reminders: List<Reminder>, checklistTemplateId: Long) {
        withContext(Dispatchers.Default) {
            launch {
                val reminderEntities = reminders.map {
                    ReminderEntity.fromDomainReminder(it, checklistTemplateId)
                }
                reminderDao.insertAll(*reminderEntities.toTypedArray())
            }
        }
    }

    private suspend fun insertTemplateCheckboxes(checkboxes: List<TemplateCheckbox>, checklistTemplateId: Long) {
        checkboxes.forEach {
            withContext(Dispatchers.Default) {
                launch {
                    val parentId =
                        templateCheckboxDao.insert(
                            TemplateCheckboxEntity.fromDomainTemplateCheckbox(
                                it,
                                checklistTemplateId
                            )
                        )
                    val children = it.children.map { child ->
                        TemplateCheckboxEntity.fromDomainTemplateCheckbox(child, checklistTemplateId)
                            .copy(parentId = parentId)
                    }
                    templateCheckboxDao.insertAll(*children.toTypedArray())
                }
            }
        }
    }

    private fun combineIntoDomainChecklistTemplate(entity: ChecklistTemplateEntity): Flow<ChecklistTemplate> {
        val checkboxesFlow = templateCheckboxDao.getAllForChecklistTemplate(entity.id)
        val checklistsFlow = checklistRoomDataSource.getBasedOn(ChecklistTemplateId(entity.id))
        val remindersFlow = reminderDao.getAllForChecklistTemplate(entity.id)
        return combine(checkboxesFlow, checklistsFlow, remindersFlow) { checkboxes, checklists, reminders ->
            mapChecklistTemplate(entity, checkboxes, checklists, reminders)
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
        val parentToChildren: Map<Long?, List<TemplateCheckboxEntity>> = checkboxes.groupBy {
            it.parentId
        }
        return parentToChildren[null]?.map { parent ->
            toDomainTemplateCheckbox(parent, null, parentToChildren[parent.checkboxId]?.map {
                toDomainTemplateCheckbox(it, TemplateCheckboxId(parent.checkboxId), listOf())
            }.orEmpty())
        }.orEmpty()
    }

    private fun toDomainTemplateCheckbox(
        templateCheckboxEntity: TemplateCheckboxEntity,
        parentId: TemplateCheckboxId?,
        children: List<TemplateCheckbox>
    ): TemplateCheckbox {
        return TemplateCheckbox(
            TemplateCheckboxId(templateCheckboxEntity.checkboxId),
            parentId,
            templateCheckboxEntity.checkboxTitle,
            children,
            templateCheckboxEntity.sortPosition
        )
    }

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
