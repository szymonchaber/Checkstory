package dev.szymonchaber.checkstory.data.migration

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
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
    private val templateRepository: TemplateRepository,
    private val synchronizer: Synchronizer,
    private val firebaseCrashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
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

    private fun toCreationCommandsDeep(template: Template): List<Command> {
        return listOf(
            TemplateCommand.CreateNewTemplate(
                template.id,
                template.createdAt.toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault()),
                UUID.randomUUID(),
                template
            ),
        ) + toCreateChecklistCommands(template.checklists)
    }

    private fun toCreateChecklistCommands(checklists: List<Checklist>): List<ChecklistCommand> {
        return checklists.map { checklist ->
            ChecklistCommand.CreateChecklistCommand(
                checklist.id,
                checklist.templateId,
                checklist.title,
                checklist.description,
                checklist.items,
                UUID.randomUUID(),
                checklist.createdAt.toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault()),
                checklist.notes,
            )
        }
    }
}

internal class CommandMigrationAttemptedWithCommandsExisting :
    IllegalStateException("Command migration attempted to run with non-empty command list")
