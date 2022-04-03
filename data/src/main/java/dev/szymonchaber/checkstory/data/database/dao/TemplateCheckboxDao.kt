package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity

@Dao
interface TemplateCheckboxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg templateCheckboxes: TemplateCheckboxEntity)

    @Delete
    fun delete(templateCheckboxDao: TemplateCheckboxEntity)
}
