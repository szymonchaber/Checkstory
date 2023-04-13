package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminderEntity")
    fun getAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminderEntity WHERE reminderEntity.templateId=:templateId")
    fun getAllForChecklistTemplate(templateId: UUID): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg reminders: ReminderEntity)

    @Query("DELETE FROM reminderEntity WHERE reminderEntity.templateId = :templateId")
    suspend fun deleteAllFromTemplate(templateId: UUID)

    @Delete
    suspend fun delete(vararg reminders: ReminderEntity)
}
