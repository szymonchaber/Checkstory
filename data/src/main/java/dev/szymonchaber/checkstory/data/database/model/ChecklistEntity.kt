package dev.szymonchaber.checkstory.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import java.time.LocalDateTime
import java.util.*

@Entity
data class ChecklistEntity(
    @PrimaryKey
    val checklistId: UUID,
    val templateId: UUID,
    val notes: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    @ColumnInfo(defaultValue = "false")
    val isRemoved: Boolean
) {

    fun toDomainChecklist(
        templateTitle: String,
        templateDescription: String,
        isTemplateRemoved: Boolean,
        tasks: List<Task>
    ): Checklist {
        return Checklist(
            id = ChecklistId(checklistId),
            templateId = TemplateId(templateId),
            title = templateTitle,
            description = templateDescription,
            items = tasks,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isRemoved = isTemplateRemoved || isRemoved
        )
    }

    companion object {

        fun fromDomainChecklist(checklist: Checklist): ChecklistEntity {
            return with(checklist) {
                ChecklistEntity(
                    checklistId = checklist.id.id,
                    templateId = templateId.id,
                    notes = notes,
                    createdAt = checklist.createdAt,
                    updatedAt = checklist.updatedAt,
                    isRemoved = checklist.isRemoved
                )
            }
        }
    }
}
