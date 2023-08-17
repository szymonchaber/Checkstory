package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.szymonchaber.checkstory.data.database.model.command.CommandEntity
import java.util.*

@Dao
interface CommandDao {

    @Query("SELECT * FROM commandEntity")
    suspend fun getAll(): List<CommandEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commandEntity: CommandEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(commands: List<CommandEntity>)

    @Delete
    suspend fun deleteAll(commands: List<CommandEntity>)

    @Query("DELETE FROM commandEntity WHERE commandEntity.id=:id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM commandEntity")
    suspend fun deleteAll()
}
