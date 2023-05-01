package dev.szymonchaber.checkstory.data.database.model.command

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class CommandEntity(
    @PrimaryKey
    val id: UUID,
    val type: String,
    val jsonData: String
)
