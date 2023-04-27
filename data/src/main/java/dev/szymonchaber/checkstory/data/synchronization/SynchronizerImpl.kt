package dev.szymonchaber.checkstory.data.synchronization

import dev.szymonchaber.checkstory.data.Event
import dev.szymonchaber.checkstory.data.State
import dev.szymonchaber.checkstory.data.api.event.CommandsApi
import dev.szymonchaber.checkstory.data.repository.LocalChecklistTemplateRepository
import dev.szymonchaber.checkstory.data.repository.RemoteChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynchronizerImpl @Inject internal constructor(
    private val checklistTemplateRepository: LocalChecklistTemplateRepository,
    private val remoteChecklistTemplateRepository: RemoteChecklistTemplateRepository,
    private val commandsApi: CommandsApi,
    private val commandRepository: CommandRepositoryImpl
) : Synchronizer {

    private val _events = mutableListOf<Event>()
    val events: List<Event>
        get() = _events

    fun checklistTitleChanged(id: String, newTitle: String) {
        _events.add(Event.TemplateTitleChanged(id, newTitle))
    }

    fun checklistTemplateCreated(id: String, title: String, description: String, tasks: List<String>) {
        _events.add(Event.TemplateCreated(id, title, description, tasks))
    }

    fun getState(): State {
        return events.fold(State()) { state, event ->
            with(event) {
                state.apply()
            }
        }
    }

    override suspend fun synchronize() {
        val commands = commandRepository.unappliedCommands
        if (commands.isEmpty()) {
            return
        }
        commandsApi.pushCommands(commands)
//        commandRepository.deleteCommands(commands.map(EditTemplateDomainCommand::commandId))
    }

    override suspend fun synchronizeCommands(editTemplateDomainEvents: List<TemplateDomainCommand>) {
        commandRepository.storeCommands(editTemplateDomainEvents)
        synchronize()
    }
}

@Singleton
class CommandRepositoryImpl @Inject constructor() {

    private val _unappliedCommands = mutableListOf<TemplateDomainCommand>()
    val unappliedCommands: List<TemplateDomainCommand>
        get() = _unappliedCommands

    val unappliedCommandsFlow = MutableStateFlow(listOf<TemplateDomainCommand>())

    suspend fun storeCommands(templateDomainCommands: List<TemplateDomainCommand>) {
        _unappliedCommands.addAll(templateDomainCommands)
        unappliedCommandsFlow.emit(_unappliedCommands)
    }

    suspend fun deleteCommands(ids: List<UUID>) {
        _unappliedCommands.removeIf { ids.contains(it.commandId) }
        unappliedCommandsFlow.emit(_unappliedCommands)
    }
}
