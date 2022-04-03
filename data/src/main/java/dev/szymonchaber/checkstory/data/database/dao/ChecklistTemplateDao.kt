package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistTemplateDao {

    @Query("SELECT * FROM checklistTemplateEntity")
    fun getAll(): Flow<List<ChecklistTemplateEntity>>

    @Query("SELECT * FROM checklistTemplateEntity WHERE id=:id")
    fun getById(id: String): Flow<ChecklistTemplateEntity>

    @Insert
    fun insertAll(vararg templates: ChecklistTemplateEntity)

    @Delete
    fun delete(template: ChecklistTemplateEntity)
}
