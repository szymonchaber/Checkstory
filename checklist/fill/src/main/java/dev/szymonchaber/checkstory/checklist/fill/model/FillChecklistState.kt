package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import java.util.*

data class FillChecklistState(val checklistLoadingState: ChecklistLoadingState) {

    companion object {

        val initial: FillChecklistState = FillChecklistState(ChecklistLoadingState.Loading)
    }
}

sealed interface ChecklistLoadingState {

    data class Success(
        val originalChecklist: Checklist,
        private val commands: List<ChecklistDomainCommand> = listOf()
    ) : ChecklistLoadingState {

        val checklist = commands.fold(originalChecklist) { checklist, command ->
            command.applyTo(checklist)
        }

        fun withUpdatedItemChecked(checkboxId: CheckboxId, isChecked: Boolean): Success {
            return plusCommand(
                ChecklistDomainCommand.ChangeTaskCheckedCommand(
                    originalChecklist.id,
                    checkboxId,
                    isChecked,
                    UUID.randomUUID(),
                    System.currentTimeMillis()
                )
            )
        }

        fun isChanged(): Boolean {
            return originalChecklist != checklist
        }

        fun withUpdatedNotes(notes: String): ChecklistLoadingState {
            return plusCommand(
                ChecklistDomainCommand.EditChecklistNotesCommand(
                    originalChecklist.id,
                    notes,
                    UUID.randomUUID(),
                    System.currentTimeMillis()
                )
            )
        }

        private fun plusCommand(command: ChecklistDomainCommand): Success {
            return copy(commands = commands.plus(command))
        }

        fun consolidatedCommands(): List<DomainCommand> {
            return consolidateCommands(commands)
        }

        private fun consolidateCommands(commands: List<ChecklistDomainCommand>): List<ChecklistDomainCommand> {
            // TODO this is a good place to trim titles and descriptions
            return commands
                .withLastCommandOfType<ChecklistDomainCommand.EditChecklistNotesCommand> {
                    it.checklistId
                }
                .withLastCommandOfType<ChecklistDomainCommand.ChangeTaskCheckedCommand> {
                    it.taskId
                }
        }

        private inline fun <reified T : ChecklistDomainCommand> List<ChecklistDomainCommand>.withLastCommandOfType(
            groupBy: (T) -> Any
        ): List<ChecklistDomainCommand> {
            val consolidatedCommand = filterIsInstance<T>()
                .groupBy(groupBy)
                .map { (_, events) ->
                    events.sortedBy { it.timestamp }.takeLast(1)
                }
                .flatten()
            val commandsWithoutConsolidatedCommand = filterNot { it is T }
            return commandsWithoutConsolidatedCommand.plus(consolidatedCommand)
        }
    }

    object Loading : ChecklistLoadingState
}
