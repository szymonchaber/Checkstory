package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class CreateChecklistFromTemplateUseCase @Inject constructor(
    private val checklistTemplateRepository: ChecklistTemplateRepository,
) {

    fun createChecklistFromTemplate(checklistTemplateId: ChecklistTemplateId): Flow<Checklist> {
        return flow {
            emit(checklistTemplateRepository.get(checklistTemplateId)!!) // TODO handle when this fails
        }
            .map { basedOn ->
                createChecklist(basedOn)
            }
    }

    private fun createChecklist(basedOn: ChecklistTemplate): Checklist {
        return with(basedOn) {
            val checklistId = ChecklistId(UUID.randomUUID())
            Checklist(
                checklistId,
                basedOn.id,
                title,
                description,
                items.map {
                    toCheckbox(it, checklistId)
                },
                "",
                LocalDateTime.now()
            )
        }
    }

    private fun toCheckbox(basedOn: TemplateCheckbox, checklistId: ChecklistId): Checkbox {
        return Checkbox(
            CheckboxId(UUID.randomUUID()),
            null,
            checklistId, // TODO we already know it from checklist
            basedOn.title,
            false,
            basedOn.children.map { child ->
                toCheckbox(child, checklistId)
            }
        )
    }
}
