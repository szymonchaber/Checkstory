package dev.szymonchaber.checkstory.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
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

    fun toTemplateTask(children: List<TemplateTask> = emptyList()): TemplateTask {
        return TemplateTask(
            id = TemplateTaskId(checkboxId),
            parentId = parentId?.let { TemplateTaskId(it) },
            title = checkboxTitle,
            children = children,
            sortPosition = sortPosition,
            templateId = TemplateId(templateId)
        )
    }

    companion object {

        fun fromDomainTemplateTask(templateTask: TemplateTask): TemplateCheckboxEntity {
            return with(templateTask) {
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
