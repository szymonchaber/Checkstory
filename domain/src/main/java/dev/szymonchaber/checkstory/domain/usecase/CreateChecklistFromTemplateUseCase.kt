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
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class CreateChecklistFromTemplateUseCase @Inject constructor(
    private val checklistTemplateRepository: ChecklistTemplateRepository,
) {

    fun createChecklistFromTemplate(checklistTemplateId: ChecklistTemplateId): Flow<Checklist> {
        return checklistTemplateRepository.get(checklistTemplateId)
            .map { basedOn ->
                createChecklist(basedOn)
            }
    }

    private fun createChecklist(basedOn: ChecklistTemplate): Checklist {
        val temporaryIdGenerator = AtomicLong(0)
        return with(basedOn) {
            Checklist(
                ChecklistId(0),
                basedOn.id,
                title,
                description,
                items.map {
                    toCheckbox(it, temporaryIdGenerator)
                },
                "",
                LocalDateTime.now()
            )
        }
    }

    private fun toCheckbox(basedOn: TemplateCheckbox, idGenerator: AtomicLong): Checkbox {
        return Checkbox(
            CheckboxId(idGenerator.getAndIncrement()),
            null,
            ChecklistId(0),
            basedOn.title,
            false,
            basedOn.children.map { child ->
                toCheckbox(child, idGenerator)
            }
        )
    }
}
