package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ChecklistTemplateDao {

    @Query(
        "SELECT * FROM checklistTemplateEntity"
    )
    fun getAll(): Flow<List<ChecklistTemplateEntity>>

    @Query(
        "SELECT * FROM checklistTemplateEntity " +
                "WHERE checklistTemplateEntity.id=:id"
    )
    fun getById(id: UUID): Flow<ChecklistTemplateEntity?>

    @Query(
        "SELECT * FROM checklistTemplateEntity " +
                "WHERE checklistTemplateEntity.id=:id"
    )
    suspend fun getByIdOrNull(id: UUID): ChecklistTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: ChecklistTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        templates: List<ChecklistTemplateEntity>,
        templateCheckboxes: List<TemplateCheckboxEntity>,
        reminders: List<ReminderEntity>
    )

    @Delete
    suspend fun delete(template: ChecklistTemplateEntity)

    @Query("DELETE from checklistTemplateEntity")
    suspend fun deleteAllTemplates()

    @Query("DELETE from templateCheckboxEntity")
    suspend fun deleteAllTemplateCheckboxes()

    @Query("DELETE from reminderEntity")
    suspend fun deleteAllReminders()

    @Transaction
    suspend fun replaceData(
        templates: List<ChecklistTemplateEntity>,
        templateCheckboxes: List<TemplateCheckboxEntity>,
        reminders: List<ReminderEntity>
    ) {
        deleteAll()
        insertAll(templates, templateCheckboxes, reminders)
    }

    @Transaction
    suspend fun deleteAll() {
        deleteAllTemplates()
        deleteAllTemplateCheckboxes()
        deleteAllReminders()
    }
}
