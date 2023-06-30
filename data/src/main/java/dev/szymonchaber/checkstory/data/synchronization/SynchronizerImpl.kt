package dev.szymonchaber.checkstory.data.synchronization

import androidx.work.WorkManager
import dev.szymonchaber.checkstory.api.checklist.ChecklistsApi
import dev.szymonchaber.checkstory.api.command.CommandsApi
import dev.szymonchaber.checkstory.api.template.TemplatesApi
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.CommandRepository
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.repository.SynchronizationResult
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynchronizerImpl @Inject internal constructor(
    private val templateRepository: TemplateRepository,
    private val commandsApi: CommandsApi,
    private val commandRepository: CommandRepository,
    private val templatesApi: TemplatesApi,
    private val checklistsApi: ChecklistsApi,
    private val checklistRepository: ChecklistRepositoryImpl,
    private val workManager: WorkManager,
    private val userRepository: UserRepository
) : Synchronizer {

    override suspend fun synchronizeCommands(commands: List<Command>) {
        commandRepository.storeCommands(commands)
        scheduleCommandsSynchronization()
    }

    override suspend fun hasUnsynchronizedCommands(): Boolean {
        return commandRepository.commandCount() > 0
    }

    override suspend fun deleteCommands() {
        commandRepository.deleteAllCommands()
    }

    override suspend fun scheduleCommandsSynchronization() {
        PushCommandsWorker.forceScheduleExpedited(workManager)
    }

    override suspend fun scheduleDataFetch() {
        FetchDataWorker.forceScheduleExpedited(workManager)
    }

    suspend fun pushCommands(): SynchronizationResult {
        val currentUser = userRepository.getCurrentUser()
        val isLoggedInPayingUser = currentUser.isLoggedIn && currentUser.isPaidUser
        if (!isLoggedInPayingUser) {
            return SynchronizationResult.Success
        }
        return try {
            val commands = commandRepository.getUnappliedCommandsFlow().first()
            commandsApi.pushCommands(commands)
            delay(5000)
            commandRepository.deleteCommands(commands.map(Command::commandId))
            SynchronizationResult.Success
        } catch (exception: Exception) {
            Timber.e(exception, "API error - skipping synchronization for now")
            SynchronizationResult.Error
        }
    }

    suspend fun fetchData(): SynchronizationResult {
        val currentUser = userRepository.getCurrentUser()
        val isLoggedInPayingUser = currentUser.isLoggedIn && currentUser.isPaidUser
        if (!isLoggedInPayingUser) {
            return SynchronizationResult.Success
        }
        return try {
            val templates = templatesApi.getTemplates()
            val checklists = checklistsApi.getChecklists()
            templateRepository.replaceData(templates)
            checklistRepository.replaceData(checklists)
            SynchronizationResult.Success
        } catch (exception: Exception) {
            Timber.e(exception, "API error - skipping synchronization for now")
            SynchronizationResult.Error
        }
    }
}
