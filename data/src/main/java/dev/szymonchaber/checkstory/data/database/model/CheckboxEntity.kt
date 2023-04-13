package dev.szymonchaber.checkstory.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import java.util.*

@Entity
data class CheckboxEntity(
    @PrimaryKey
    val checkboxId: UUID,
    val checklistId: UUID,
    val checkboxTitle: String,
    val isChecked: Boolean,
    val parentId: UUID?
) {

    fun toDomainCheckbox(children: List<Checkbox>): Checkbox {
        return Checkbox(
            CheckboxId(checkboxId),
            parentId?.let(::CheckboxId),
            ChecklistId(checklistId),
            checkboxTitle,
            isChecked,
            children
        )
    }

    companion object {

        fun fromDomainCheckbox(checkbox: Checkbox): CheckboxEntity {
            return with(checkbox) {
                CheckboxEntity(
                    checkboxId = id.id,
                    checklistId = checklistId.id,
                    checkboxTitle = title,
                    isChecked = isChecked,
                    parentId = parentId?.id
                )
            }
        }
    }
}
