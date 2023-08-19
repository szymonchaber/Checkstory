package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface TemplateDao {

    @Query(
        "SELECT * FROM checklistTemplateEntity"
    )
    fun getAll(): Flow<List<ChecklistTemplateEntity>>

    @Query(
        "SELECT * FROM checklistTemplateEntity"
    )
    fun getAllDeep(): Flow<List<DeepTemplateEntity>>

    @Query("SELECT * FROM reminderEntity WHERE reminderEntity.templateId=:templateId")
    fun getAllRemindersForTemplate(templateId: UUID): Flow<List<ReminderEntity>>

    @Query(
        "SELECT * FROM checklistTemplateEntity " +
                "WHERE checklistTemplateEntity.id=:id"
    )
    fun getById(id: UUID): Flow<ChecklistTemplateEntity?>

    @Query(
        "SELECT * FROM checklistTemplateEntity " +
                "WHERE checklistTemplateEntity.id=:id"
    )
    suspend fun getByIdOrNull(id: UUID): DeepTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: ChecklistTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(templateCheckboxes: List<TemplateCheckboxEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        templates: List<ChecklistTemplateEntity>,
        templateTasks: List<TemplateCheckboxEntity>,
        reminders: List<ReminderEntity>
    )

    @Delete
    suspend fun delete(template: ChecklistTemplateEntity)

    @Transaction
    suspend fun deleteAll() {
        deleteAllTemplates()
        deleteAllTemplateTasks()
        deleteAllReminders()
    }

    @Query("DELETE from checklistTemplateEntity")
    suspend fun deleteAllTemplates()

    @Query("DELETE from templateCheckboxEntity")
    suspend fun deleteAllTemplateTasks()

    @Query("DELETE from reminderEntity")
    suspend fun deleteAllReminders()

    @Transaction
    suspend fun insert(
        template: ChecklistTemplateEntity,
        checkboxes: List<TemplateCheckboxEntity>,
        reminders: List<ReminderEntity>
    ) {
        insert(template)
        insertAllTasks(checkboxes)
        insertReminders(reminders)
    }
}
