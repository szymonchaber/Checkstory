package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

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
    fun getAll(templateId: UUID): Flow<List<ChecklistEntity>>

    @Query(
        "SELECT * FROM checklistEntity WHERE checklistEntity.checklistId=:id"
    )
    fun getById(id: UUID): Flow<ChecklistEntity?>

    @Query(
        "SELECT * FROM checklistEntity WHERE checklistEntity.checklistId=:id"
    )
    suspend fun getByIdOrNull(id: UUID): ChecklistEntity?

    @Query("SELECT * FROM checkboxEntity WHERE checkboxEntity.checklistId=:checklistId")
    fun getCheckboxesForChecklist(checklistId: UUID): Flow<List<CheckboxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checklistEntity: ChecklistEntity)

    @Update
    suspend fun update(checklist: ChecklistEntity)

    @Delete
    suspend fun delete(checklistEntity: ChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checklists: List<ChecklistEntity>, checkboxes: List<CheckboxEntity>)

    @Query("DELETE FROM checklistentity")
    suspend fun deleteAllChecklists()

    @Query("DELETE FROM checkboxEntity")
    suspend fun deleteAllCheckboxes()

    @Transaction
    suspend fun replaceData(checklists: List<ChecklistEntity>, checkboxes: List<CheckboxEntity>) {
        deleteAllData()
        insertAll(checklists, checkboxes)
    }

    @Transaction
    suspend fun deleteAllData() {
        deleteAllChecklists()
        deleteAllCheckboxes()
    }
}
