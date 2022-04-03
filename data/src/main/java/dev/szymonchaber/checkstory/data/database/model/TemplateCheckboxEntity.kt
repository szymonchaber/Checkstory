package dev.szymonchaber.checkstory.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TemplateCheckboxEntity(
    @PrimaryKey(autoGenerate = true)
    val checkboxId: Long,
    val templateId: Long,
    val checkboxTitle: String
)