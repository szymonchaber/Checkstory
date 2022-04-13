package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query(
        "SELECT * FROM checklistEntity " +
                "ORDER BY checklistEntity.createdAt DESC"
    )
    fun getAll(): Flow<List<ChecklistEntity>>

    @Query(
        "SELECT * FROM checklistEntity " +
                "WHERE checklistEntity.templateId=:templateId " +
                "ORDER BY checklistEntity.createdAt DESC"
    )
    fun getAll(templateId: Long): Flow<List<ChecklistEntity>>

    @Query(
        "SELECT * FROM checklistEntity " +
                "WHERE checklistEntity.checklistId=:id"
    )
    fun getById(id: Long): Flow<ChecklistEntity?>

    @Query("SELECT * FROM checkboxEntity WHERE checkboxEntity.checklistId=:checklistId")
    fun getCheckboxesForChecklist(checklistId: Long): Flow<List<CheckboxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checklistEntity: ChecklistEntity): Long

    @Update
    suspend fun update(checklist: ChecklistEntity)

    @Delete
    suspend fun delete(checklistEntity: ChecklistEntity)
}
