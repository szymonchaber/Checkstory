package dev.szymonchaber.checkstory.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.time.LocalDateTime
import java.util.*

@Entity
data class ChecklistEntity(
    @PrimaryKey
    val checklistId: UUID,
    val templateId: UUID,
    val notes: String,
    val createdAt: LocalDateTime
) {

    fun toDomainChecklist(
        templateTitle: String,
        templateDescription: String,
        checkboxes: List<Checkbox>
    ): Checklist {
        return Checklist(
            ChecklistId(checklistId),
            ChecklistTemplateId(templateId),
            templateTitle,
            templateDescription,
            checkboxes,
            notes,
            createdAt
        )
    }

    companion object {

        fun fromDomainChecklist(checklist: Checklist): ChecklistEntity {
            return with(checklist) {
                ChecklistEntity(
                    checklistId = checklist.id.id,
                    templateId = checklistTemplateId.id,
                    notes = notes,
                    checklist.createdAt
                )
            }
        }
    }
}
