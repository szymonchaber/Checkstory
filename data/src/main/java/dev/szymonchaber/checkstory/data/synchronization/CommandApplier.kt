package dev.szymonchaber.checkstory.data.synchronization

import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import javax.inject.Inject

internal class CommandApplier @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val templateRepository: TemplateRepository
) {

    suspend fun applyCommandsToLocalData(commands: List<Command>) {
        applyTemplateCommands(commands)
        applyChecklistCommands(commands)
    }

    private suspend fun applyTemplateCommands(commands: List<Command>) {
        commands.filterIsInstance<TemplateCommand>()
            .groupBy {
                it.templateId
            }
            .forEach { (id, commands) ->
                val template = templateRepository.get(id)?.let {
                    commands.applyAllTo(it)
                } ?: attemptCommandOnlyCreation(id, commands)
                template?.let {
                    templateRepository.save(it)
                }
            }
    }

    private suspend fun applyChecklistCommands(commands: List<Command>) {
        commands.filterIsInstance<ChecklistCommand>()
            .groupBy {
                it.checklistId
            }.forEach { (id, commands) ->
                val checklist = checklistRepository.get(id)?.let {
                    commands.applyAllTo(it)
                } ?: attemptCommandOnlyCreation(id, commands)
                checklist?.let {
                    checklistRepository.save(it)
                }
            }
    }

    private fun attemptCommandOnlyCreation(id: TemplateId, commands: List<TemplateCommand>): Template? {
        return if (commands.any { it is TemplateCommand.CreateNewTemplate }) {
            commands.applyAllTo(Template.empty(id))
        } else {
            null
        }
    }

    private fun attemptCommandOnlyCreation(id: ChecklistId, commands: List<ChecklistCommand>): Checklist? {
        return if (commands.any { it is ChecklistCommand.CreateChecklistCommand }) {
            commands.applyAllTo(Checklist.empty(id))
        } else {
            null
        }
    }

    private fun List<TemplateCommand>.applyAllTo(template: Template): Template {
        return fold(template) { foldedTemplate, command ->
            command.applyTo(foldedTemplate)
        }
    }

    private fun List<ChecklistCommand>.applyAllTo(template: Checklist): Checklist {
        return fold(template) { foldedChecklist, command ->
            command.applyTo(foldedChecklist)
        }
    }
}
