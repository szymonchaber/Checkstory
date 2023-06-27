package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Embedded
import androidx.room.Relation
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity

data class DeepChecklistEntity(
    @Embedded val checklist: ChecklistEntity,
    @Relation(
        parentColumn = "templateId",
        entityColumn = "id"
    )
    val template: ChecklistTemplateEntity,
    @Relation(
        parentColumn = "checklistId",
        entityColumn = "checklistId"
    )
    val tasks: List<CheckboxEntity>
)
