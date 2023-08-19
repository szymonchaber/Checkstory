package dev.szymonchaber.checkstory.data.synchronization

import androidx.room.Dao
import androidx.room.Transaction
import dev.szymonchaber.checkstory.data.database.AppDatabase
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
abstract class SynchronizationDao(private val appDatabase: AppDatabase) {

    private val templateDao = appDatabase.templateDao
    private val checklistDao = appDatabase.checklistDao

    @Transaction
    open suspend fun replaceData(newTemplates: List<Template>, newChecklists: List<Checklist>) {
        checklistDao.deleteAllData()
        templateDao.deleteAll()

        withContext(Dispatchers.Default) {
            replaceTemplates(newTemplates)
            replaceChecklists(newChecklists)
        }
    }

    private suspend fun replaceTemplates(newTemplates: List<Template>) {
        val templates = newTemplates.map(ChecklistTemplateEntity::fromDomainTemplate)
        val flatItems =
            newTemplates.flatMap(Template::flattenedTasks).map(TemplateCheckboxEntity::fromDomainTemplateTask)
        val flatReminders = newTemplates.flatMap(Template::reminders).map(ReminderEntity::fromDomainReminder)
        templateDao.insertAll(templates, flatItems, flatReminders)
    }

    private suspend fun replaceChecklists(newChecklists: List<Checklist>) {
        val checklists = newChecklists.map(ChecklistEntity::fromDomainChecklist)
        val flatTasks = newChecklists.flatMap(Checklist::flattenedItems).map(CheckboxEntity::fromDomainTask)
        checklistDao.insertAll(checklists, flatTasks)
    }
}
