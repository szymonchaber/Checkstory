package dev.szymonchaber.checkstory.data.migration

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import java.util.*
import javax.inject.Inject

class CommandModelMigration @Inject internal constructor(
    private val migrationPreferences: MigrationPreferences,
    private val templateRepository: ChecklistTemplateRepository,
    private val synchronizer: Synchronizer,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val uuidGenerator: () -> UUID = { UUID.randomUUID() }
) {

    suspend fun run() {
        if (migrationPreferences.didRunCommandModelMigration()) {
            return
        }
        if (synchronizer.hasUnsynchronizedCommands()) {
            firebaseCrashlytics
                .recordException(CommandMigrationAttemptedWithCommandsExisting())
            return
        }
        runActual()
    }

    private suspend fun runActual() {
        withContext(Dispatchers.Default) {
            val templates = templateRepository.getAll().first()
            templates
                .flatMap {
                    toCreationCommandsDeep(it)
                }
                .ifEmpty { null }
                ?.let {
                    synchronizer.synchronizeCommands(it)
                }
            migrationPreferences.markDidRunCommandModelMigration()
        }
    }

    private fun toCreationCommandsDeep(checklistTemplate: ChecklistTemplate): List<Command> {
        return listOf(
            TemplateCommand.CreateNewTemplate(
                checklistTemplate.id,
                checklistTemplate.createdAt.toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault()),
                uuidGenerator(),
                checklistTemplate
            ),
        ) + toCreateChecklistCommands(checklistTemplate.checklists)
    }

    private fun toCreateChecklistCommands(checklists: List<Checklist>): List<ChecklistCommand> {
        return checklists.map { checklist ->
            ChecklistCommand.CreateChecklistCommand(
                checklist.id,
                checklist.checklistTemplateId,
                checklist.title,
                checklist.description,
                checklist.items,
                uuidGenerator(),
                checklist.createdAt.toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault()),
                checklist.notes,
            )
        }
    }
}

internal class CommandMigrationAttemptedWithCommandsExisting :
    IllegalStateException("Command migration attempted to run with non-empty command list")
