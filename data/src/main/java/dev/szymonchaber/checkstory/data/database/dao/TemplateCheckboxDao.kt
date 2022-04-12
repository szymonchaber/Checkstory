package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity

@Dao
interface TemplateCheckboxDao {

    @Query("SELECT * FROM templateCheckboxEntity WHERE templateCheckboxEntity.checkboxId = :checkboxId")
    suspend fun getById(checkboxId: Long): TemplateCheckboxEntity

    @Insert
    suspend fun insert(templateCheckbox: TemplateCheckboxEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg templateCheckboxes: TemplateCheckboxEntity)

    @Delete
    suspend fun delete(templateCheckboxDao: TemplateCheckboxEntity)
}