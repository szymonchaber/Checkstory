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
    fun getAll(): Flow<List<DeepChecklistEntity>>

    @Query(
        "SELECT * FROM checklistEntity " +
                "WHERE checklistEntity.templateId=:templateId " +
                "ORDER BY checklistEntity.createdAt DESC"
    )
    fun getAllForTemplate(templateId: UUID): Flow<List<DeepChecklistEntity>>

    @Query(
        "SELECT * FROM checklistEntity WHERE checklistEntity.checklistId=:id"
    )
    suspend fun getByIdOrNull(id: UUID): DeepChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checklistEntity: ChecklistEntity)

    @Update
    suspend fun update(checklist: ChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checklists: List<ChecklistEntity>, checkboxes: List<CheckboxEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<CheckboxEntity>)

    @Query("DELETE FROM checklistentity")
    suspend fun deleteAllChecklists()

    @Query("DELETE FROM checkboxEntity")
    suspend fun deleteAllTasks()

    @Transaction
    suspend fun replaceData(checklists: List<ChecklistEntity>, tasks: List<CheckboxEntity>) {
        deleteAllData()
        insertAll(checklists, tasks)
    }

    @Transaction
    suspend fun deleteAllData() {
        deleteAllChecklists()
        deleteAllTasks()
    }
}
