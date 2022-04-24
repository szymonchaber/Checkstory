package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateCheckboxDao {

    @Query("SELECT * FROM templateCheckboxEntity WHERE templateCheckboxEntity.checkboxId = :checkboxId")
    suspend fun getById(checkboxId: Long): TemplateCheckboxEntity

    @Query("SELECT * FROM templateCheckboxEntity WHERE templateCheckboxEntity.templateId=:templateId")
    fun getAllForChecklistTemplate(templateId: Long): Flow<List<TemplateCheckboxEntity>>

    @Insert
    suspend fun insert(templateCheckbox: TemplateCheckboxEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg templateCheckboxes: TemplateCheckboxEntity)

    @Query(
        "DELETE FROM templateCheckboxEntity " +
                "WHERE templateCheckboxEntity.checkboxId = :templateCheckboxId " +
                "OR templateCheckboxEntity.parentId = :templateCheckboxId"
    )
    suspend fun deleteCascading(templateCheckboxId: Long)

    @Query("DELETE FROM templateCheckboxEntity WHERE templateCheckboxEntity.templateId = :templateId")
    suspend fun deleteAllFromTemplate(templateId: Long)
}
