package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import kotlinx.datetime.Clock
import java.util.*

sealed interface FillChecklistState {

    data object Loading : FillChecklistState

    data class Success(
        val originalChecklist: Checklist,
        private val commands: List<ChecklistCommand> = listOf()
    ) : FillChecklistState {

        val checklist = commands.fold(originalChecklist) { checklist, command ->
            command.applyTo(checklist)
        }

        fun withUpdatedItemChecked(taskId: TaskId, isChecked: Boolean): Success {
            return plusCommand(
                ChecklistCommand.ChangeTaskCheckedCommand(
                    originalChecklist.id,
                    taskId,
                    isChecked,
                    UUID.randomUUID(),
                    Clock.System.now()
                )
            )
        }

        fun isChanged() = commands.isNotEmpty()

        fun withUpdatedNotes(notes: String): FillChecklistState {
            return plusCommand(
                ChecklistCommand.EditChecklistNotesCommand(
                    originalChecklist.id,
                    notes,
                    UUID.randomUUID(),
                    Clock.System.now()
                )
            )
        }

        private fun plusCommand(command: ChecklistCommand): Success {
            return copy(commands = commands.plus(command))
        }

        fun consolidatedCommands(): List<Command> {
            return consolidateCommands(commands)
        }

        private fun consolidateCommands(commands: List<ChecklistCommand>): List<ChecklistCommand> {
            // TODO this is a good place to trim titles and descriptions
            return commands
                .withLastCommandOfType<ChecklistCommand.EditChecklistNotesCommand> {
                    it.checklistId
                }
                .withLastCommandOfType<ChecklistCommand.ChangeTaskCheckedCommand> {
                    it.taskId
                }
        }

        private inline fun <reified T : ChecklistCommand> List<ChecklistCommand>.withLastCommandOfType(
            groupBy: (T) -> Any
        ): List<ChecklistCommand> {
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
}
