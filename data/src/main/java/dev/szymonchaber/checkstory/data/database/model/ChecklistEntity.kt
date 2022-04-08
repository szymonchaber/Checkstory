package dev.szymonchaber.checkstory.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@Entity
data class ChecklistEntity(
    @PrimaryKey(autoGenerate = true)
    val checklistId: Long,
    val templateId: Long,
    val notes: String,
) {

    fun toDomainChecklist(
        templateTitle: String,
        templateDescription: String,
        checkboxes: List<Checkbox>
    ): Checklist {
        return Checklist(
            ChecklistId(checklistId.toString()),
            ChecklistTemplateId(templateId),
            templateTitle,
            templateDescription,
            checkboxes,
            notes
        )
    }

    companion object {

        fun fromDomainChecklist(checklist: Checklist): ChecklistEntity {
            return with(checklist) {
                ChecklistEntity(
                    checklistId = checklist.id.id.toLong(),
                    templateId = checklistTemplateId.id,
                    notes = notes
                )
            }
        }
    }
}
