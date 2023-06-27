package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity

@Dao
interface TaskDao {

    @Query("SELECT * FROM checkboxEntity WHERE checkboxEntity.checkboxId = :checkboxId")
    suspend fun getById(checkboxId: Long): CheckboxEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tasks: List<CheckboxEntity>)
}
