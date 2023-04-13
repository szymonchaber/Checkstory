package dev.szymonchaber.checkstory.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import java.util.*

@Entity
data class TemplateCheckboxEntity(
    @PrimaryKey
    val checkboxId: UUID,
    val templateId: UUID,
    val checkboxTitle: String,
    val parentId: UUID?,
    @ColumnInfo(defaultValue = "0")
    val sortPosition: Long
) {

    companion object {

        fun fromDomainTemplateCheckbox(
            templateCheckbox: TemplateCheckbox,
            templateId: UUID
        ): TemplateCheckboxEntity {
            return with(templateCheckbox) {
                TemplateCheckboxEntity(
                    checkboxId = id.id,
                    parentId = templateCheckbox.parentId?.id,
                    templateId = templateId,
                    checkboxTitle = title,
                    sortPosition = sortPosition
                )
            }
        }
    }
}
