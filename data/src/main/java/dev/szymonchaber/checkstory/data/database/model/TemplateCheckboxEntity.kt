package dev.szymonchaber.checkstory.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox

@Entity
data class TemplateCheckboxEntity(
    @PrimaryKey(autoGenerate = true)
    val checkboxId: Long,
    val templateId: Long,
    val checkboxTitle: String,
    val parentId: Long?
) {

    companion object {

        fun fromDomainTemplateCheckbox(
            templateCheckbox: TemplateCheckbox,
            templateId: Long
        ): TemplateCheckboxEntity {
            return with(templateCheckbox) {
                TemplateCheckboxEntity(
                    checkboxId = id.id,
                    parentId = templateCheckbox.parentId?.id,
                    templateId = templateId,
                    checkboxTitle = title
                )
            }
        }
    }
}
