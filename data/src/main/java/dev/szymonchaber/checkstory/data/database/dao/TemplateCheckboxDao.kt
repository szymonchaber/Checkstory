package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface TemplateCheckboxDao {

    @Query("SELECT * FROM templateCheckboxEntity WHERE templateCheckboxEntity.checkboxId = :checkboxId")
    suspend fun getById(checkboxId: Long): TemplateCheckboxEntity

    @Query(
        "SELECT * FROM templateCheckboxEntity WHERE templateCheckboxEntity.templateId=:templateId " +
                "ORDER BY templateCheckboxEntity.sortPosition ASC"
    )
    fun getAllForChecklistTemplate(templateId: UUID): Flow<List<TemplateCheckboxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(templateCheckbox: TemplateCheckboxEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg templateCheckboxes: TemplateCheckboxEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templateCheckboxes: List<TemplateCheckboxEntity>)

    @Query(
        "DELETE FROM templateCheckboxEntity " +
                "WHERE templateCheckboxEntity.checkboxId = :templateCheckboxId " +
                "OR templateCheckboxEntity.parentId = :templateCheckboxId"
    )
    suspend fun deleteCascading(templateCheckboxId: UUID)

    @Query("DELETE FROM templateCheckboxEntity WHERE templateCheckboxEntity.templateId = :templateId")
    suspend fun deleteAllFromTemplate(templateId: UUID)
}
