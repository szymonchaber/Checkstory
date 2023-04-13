package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
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

    @Delete
    suspend fun delete(template: ChecklistTemplateEntity)
}
