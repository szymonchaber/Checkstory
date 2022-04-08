package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {

    @Query(
        "SELECT * FROM checklistEntity " +
                "JOIN checkboxEntity ON checklistEntity.checklistId = checkboxEntity.checklistId"
    )
    fun getAll(): Flow<Map<ChecklistEntity, List<CheckboxEntity>>>

    @Query(
        "SELECT * FROM checklistEntity " +
                "JOIN checkboxEntity ON checklistEntity.checklistId = checkboxEntity.checklistId " +
                "WHERE checklistEntity.checklistId=:id"
    )
    fun getById(id: Long): Flow<Map<ChecklistEntity, List<CheckboxEntity>>>

    @Insert
    suspend fun insert(checklistEntity: ChecklistEntity): Long

    @Update
    suspend fun update(checklist: ChecklistEntity)
}
