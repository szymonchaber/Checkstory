package dev.szymonchaber.checkstory.data.synchronization

import androidx.work.WorkManager
import dev.szymonchaber.checkstory.api.checklist.ChecklistsApi
import dev.szymonchaber.checkstory.api.command.CommandsApi
import dev.szymonchaber.checkstory.api.template.TemplatesApi
import dev.szymonchaber.checkstory.data.repository.CommandRepository
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.repository.SynchronizationResult
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynchronizerImpl @Inject internal constructor(
    private val commandsApi: CommandsApi,
    private val commandRepository: CommandRepository,
    private val templatesApi: TemplatesApi,
    private val checklistsApi: ChecklistsApi,
    private val workManager: WorkManager,
    private val userRepository: UserRepository,
    private val commandApplier: CommandApplier,
    private val synchronizationDao: SynchronizationDao
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
        PushCommandsWorker.scheduleExpedited(workManager)
    }

    override suspend fun scheduleDataFetch() {
        FetchDataWorker.scheduleExpedited(workManager)
    }

    override suspend fun synchronizeManually() {
        pushCommands()
        fetchData()
    }

    suspend fun pushCommands(): SynchronizationResult {
        mutex.withLock {
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
        }
    }

    suspend fun fetchData(): SynchronizationResult {
        mutex.withLock {
            val currentUser = userRepository.getCurrentUser()
            val isLoggedInPayingUser = currentUser.isLoggedIn && currentUser.isPaidUser
            if (!isLoggedInPayingUser) {
                return SynchronizationResult.Success
            }
            return try {
                withContext(Dispatchers.IO) {
                    val templates = async {
                        templatesApi.getTemplates()
                    }
                    val checklists = async {
                        checklistsApi.getChecklists()
                    }
                    synchronizationDao.replaceData(templates.await(), checklists.await())
                }
                SynchronizationResult.Success
            } catch (exception: Exception) {
                Timber.e(exception, "API error - skipping synchronization for now")
                SynchronizationResult.Error
            }
        }
    }
}
