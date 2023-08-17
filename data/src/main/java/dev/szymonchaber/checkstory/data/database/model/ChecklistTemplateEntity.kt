package dev.szymonchaber.checkstory.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import java.time.LocalDateTime
import java.util.*

@Entity
data class ChecklistTemplateEntity(
    @PrimaryKey
    val id: UUID,
    val title: String,
    val description: String,
    val createdAt: LocalDateTime,
    @ColumnInfo(defaultValue = "false")
    val isRemoved: Boolean
) {

    companion object {

        fun fromDomainTemplate(template: Template): ChecklistTemplateEntity {
            return with(template) {
                ChecklistTemplateEntity(
                    id = id.id,
                    title = title,
                    description = description,
                    createdAt = createdAt,
                    isRemoved = isRemoved
                )
            }
        }
    }
}
