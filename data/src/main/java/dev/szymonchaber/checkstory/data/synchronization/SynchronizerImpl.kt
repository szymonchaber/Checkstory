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
import kotlinx.coroutines.sync.Mutex
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
    private val userRepository: UserRepository,
    private val commandApplier: CommandApplier
) : Synchronizer {

    private val mutex = Mutex()

    override suspend fun storeCommands(commands: List<Command>) {
        commandRepository.storeCommands(commands)
        commandApplier.applyCommandsToLocalData(commands)
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

    override suspend fun synchronizeManually() {
        pushCommands()
        fetchData()
    }

    suspend fun pushCommands(): SynchronizationResult {
        if (!mutex.tryLock()) {
            Timber.d("Another synchronization is in progress, cancelling this command push attempt.")
            return SynchronizationResult.Success
        }
        try {
            val currentUser = userRepository.getCurrentUser()
            val isLoggedInPayingUser = currentUser.isLoggedIn && currentUser.isPaidUser
            if (!isLoggedInPayingUser) {
                return SynchronizationResult.Success
            }
            return try {
                val commands = commandRepository.getUnsynchronizedCommands()
                commandsApi.pushCommands(commands)
                commandRepository.deleteCommands(commands.map(Command::commandId))
                SynchronizationResult.Success
            } catch (exception: Exception) {
                Timber.e(exception, "API error - skipping synchronization for now")
                SynchronizationResult.Error
            }
        } finally {
            mutex.unlock()
        }
    }

    suspend fun fetchData(): SynchronizationResult {
        if (!mutex.tryLock()) {
            Timber.d("Another synchronization is in progress, cancelling this data fetch attempt.")
            return SynchronizationResult.Success
        }
        try {
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
        } finally {
            mutex.unlock()
        }
    }
}
