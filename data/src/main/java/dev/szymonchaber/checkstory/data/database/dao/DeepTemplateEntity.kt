package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Embedded
import androidx.room.Relation
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity

data class DeepTemplateEntity(
    @Embedded val template: ChecklistTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val reminders: List<ReminderEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val tasks: List<TemplateCheckboxEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId",
        entity = ChecklistEntity::class
    )
    val checklists: List<DeepChecklistEntity>
)
