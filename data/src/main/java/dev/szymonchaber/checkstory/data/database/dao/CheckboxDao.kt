package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity

@Dao
interface CheckboxDao {

    @Query("SELECT * FROM checkboxEntity WHERE checkboxEntity.checkboxId = :checkboxId")
    suspend fun getById(checkboxId: Long): CheckboxEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(checkboxes: List<CheckboxEntity>)

    @Delete
    suspend fun delete(vararg checkbox: CheckboxEntity)
}
