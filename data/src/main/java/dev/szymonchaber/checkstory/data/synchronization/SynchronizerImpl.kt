package dev.szymonchaber.checkstory.data.synchronization

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.data.api.event.ChecklistsApi
import dev.szymonchaber.checkstory.data.api.event.CommandsApi
import dev.szymonchaber.checkstory.data.api.event.TemplatesApi
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.ChecklistTemplateRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.CommandRepositoryImpl
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynchronizerImpl @Inject internal constructor(
    private val checklistTemplateRepository: ChecklistTemplateRepositoryImpl,
    private val commandsApi: CommandsApi,
    private val commandRepository: CommandRepositoryImpl,
    private val templatesApi: TemplatesApi,
    private val checklistsApi: ChecklistsApi,
    private val checklistRepository: ChecklistRepositoryImpl
) : Synchronizer {

    override suspend fun synchronizeCommands(commands: List<DomainCommand>) {
        commandRepository.storeCommands(commands)
        synchronize()
    }

    override suspend fun synchronize() {
        if (Firebase.auth.currentUser == null) {
            return
        }
        val commands = commandRepository.unappliedCommandsFlow.first()
        if (commands.isNotEmpty()) {
            commandsApi.pushCommands(commands)
            commandRepository.deleteCommands(commands.map(DomainCommand::commandId))
        }
        val templates = templatesApi.getTemplates()
        val checklists = checklistsApi.getChecklists()
        checklistTemplateRepository.replaceData(templates)

        checklistRepository.replaceData(checklists)
    }
}
