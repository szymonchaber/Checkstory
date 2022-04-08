package dev.szymonchaber.checkstory.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate

@Entity
data class ChecklistTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val description: String,
) {

    companion object {

        fun fromDomainChecklistTemplate(checklistTemplate: ChecklistTemplate): ChecklistTemplateEntity {
            return with(checklistTemplate) {
                ChecklistTemplateEntity(
                    id = checklistTemplate.id.id.toLong(),
                    title = title,
                    description = description
                )
            }
        }
    }
}
