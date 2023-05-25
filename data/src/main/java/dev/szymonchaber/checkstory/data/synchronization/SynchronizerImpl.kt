package dev.szymonchaber.checkstory.data.synchronization

import androidx.work.WorkManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.data.api.event.ChecklistsApi
import dev.szymonchaber.checkstory.data.api.event.CommandsApi
import dev.szymonchaber.checkstory.data.api.event.TemplatesApi
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.ChecklistTemplateRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.CommandRepositoryImpl
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.repository.SynchronizationResult
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynchronizerImpl @Inject internal constructor(
    private val checklistTemplateRepository: ChecklistTemplateRepositoryImpl,
    private val commandsApi: CommandsApi,
    private val commandRepository: CommandRepositoryImpl,
    private val templatesApi: TemplatesApi,
    private val checklistsApi: ChecklistsApi,
    private val checklistRepository: ChecklistRepositoryImpl,
    private val workManager: WorkManager
) : Synchronizer {

    override suspend fun synchronizeCommands(commands: List<DomainCommand>) {
        commandRepository.storeCommands(commands)
        scheduleSynchronization()
    }

    override suspend fun hasUnsynchronizedCommands(): Boolean {
        return commandRepository.commandCount() > 0
    }

    override suspend fun deleteCommands() {
        commandRepository.deleteAllCommands()
    }

    override suspend fun scheduleSynchronization() {
        SynchronizationWorker.schedule(workManager)
    }

    suspend fun performSynchronization(): SynchronizationResult {
        if (Firebase.auth.currentUser == null) {
            return SynchronizationResult.Success
        }
        return try {
            val commands = commandRepository.unappliedCommandsFlow.first()
            commandsApi.pushCommands(commands)
            val templates = templatesApi.getTemplates()
            val checklists = checklistsApi.getChecklists()
            checklistTemplateRepository.replaceData(templates)
            checklistRepository.replaceData(checklists)
            commandRepository.deleteCommands(commands.map(DomainCommand::commandId))
            SynchronizationResult.Success
        } catch (exception: Exception) {
            Timber.e("API error - skipping synchronization for now", exception)
            SynchronizationResult.Error
        }
    }
}
