package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.*
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity

@Dao
interface CheckboxDao {

    @Query("SELECT * FROM checkboxEntity WHERE checkboxEntity.checkboxId = :checkboxId")
    suspend fun getById(checkboxId: Long): CheckboxEntity

    @Insert
    suspend fun insert(checkbox: CheckboxEntity): Long

    @Update
    suspend fun update(checkbox: CheckboxEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg checkboxes: CheckboxEntity)

    @Delete
    suspend fun delete(vararg checkbox: CheckboxEntity)
}
