package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistTemplateDao {

    @Query(
        "SELECT * FROM checklistTemplateEntity " +
                "JOIN templateCheckboxEntity ON checklistTemplateEntity.id = templateCheckboxEntity.templateId"
    )
    fun getAll(): Flow<Map<ChecklistTemplateEntity, List<TemplateCheckboxEntity>>>

    @Query(
        "SELECT * FROM checklistTemplateEntity " +
                "JOIN templateCheckboxEntity ON checklistTemplateEntity.id = templateCheckboxEntity.templateId " +
                "WHERE checklistTemplateEntity.id=:id "
    )
    fun getById(id: Long): Flow<Map<ChecklistTemplateEntity, List<TemplateCheckboxEntity>>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: ChecklistTemplateEntity): Long

    @Delete
    suspend fun delete(template: ChecklistTemplateEntity)
}
