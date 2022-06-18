package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import kotlinx.coroutines.flow.Flow

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
    fun getById(id: Long): Flow<ChecklistTemplateEntity?>

    @Query(
        "SELECT * FROM checklistTemplateEntity " +
                "WHERE checklistTemplateEntity.id=:id"
    )
    suspend fun getByIdOrNull(id: Long): ChecklistTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: ChecklistTemplateEntity): Long

    @Delete
    suspend fun delete(template: ChecklistTemplateEntity)
}
