package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class CreateChecklistFromTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {

    fun createChecklistFromTemplate(templateId: TemplateId): Flow<Checklist> {
        return flow {
            emit(templateRepository.get(templateId)!!) // TODO handle when this fails
        }
            .map { basedOn ->
                createChecklist(basedOn)
            }
    }

    private fun createChecklist(basedOn: Template): Checklist {
        return with(basedOn) {
            val checklistId = ChecklistId.new()
            Checklist(
                checklistId,
                basedOn.id,
                title,
                description,
                tasks.map {
                    toTask(it, checklistId)
                }.sortedBy(Task::sortPosition),
                "",
                LocalDateTime.now()
            )
        }
    }

    private fun toTask(
        basedOn: TemplateTask,
        checklistId: ChecklistId,
        parentId: TaskId? = null
    ): Task {
        val id = TaskId(UUID.randomUUID())
        return Task(
            id,
            parentId = parentId,
            checklistId = checklistId,
            title = basedOn.title,
            isChecked = false,
            children = basedOn.children.map { child ->
                toTask(child, checklistId, id)
            }.sortedBy(Task::sortPosition),
            sortPosition = basedOn.sortPosition
        )
    }
}
