package dev.szymonchaber.checkstory.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
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

    fun toTemplateCheckbox(children: List<TemplateCheckbox> = emptyList()): TemplateCheckbox {
        return TemplateCheckbox(
            id = TemplateCheckboxId(checkboxId),
            parentId = parentId?.let { TemplateCheckboxId(it) },
            title = checkboxTitle,
            children = children,
            sortPosition = sortPosition,
            templateId = TemplateId(templateId)
        )
    }

    companion object {

        fun fromDomainTemplateCheckbox(templateCheckbox: TemplateCheckbox): TemplateCheckboxEntity {
            return with(templateCheckbox) {
                TemplateCheckboxEntity(
                    checkboxId = id.id,
                    parentId = parentId?.id,
                    templateId = templateId.id,
                    checkboxTitle = title,
                    sortPosition = sortPosition
                )
            }
        }
    }
}
