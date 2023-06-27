package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import java.util.*

@Dao
interface TemplateTaskDao {

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
