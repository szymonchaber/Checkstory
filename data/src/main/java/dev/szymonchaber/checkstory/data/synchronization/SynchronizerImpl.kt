package dev.szymonchaber.checkstory.data.synchronization

import dev.szymonchaber.checkstory.data.Event
import dev.szymonchaber.checkstory.data.State
import dev.szymonchaber.checkstory.data.api.event.CommandsApi
import dev.szymonchaber.checkstory.data.repository.LocalChecklistTemplateRepository
import dev.szymonchaber.checkstory.data.repository.RemoteChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
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

    override suspend fun synchronizeCommands(commands: List<DomainCommand>) {
        commandRepository.storeCommands(commands)
        synchronize()
    }
}

@Singleton
class CommandRepositoryImpl @Inject constructor() {

    private val _unappliedCommands = mutableListOf<DomainCommand>()
    val unappliedCommands: List<DomainCommand>
        get() = _unappliedCommands

    val unappliedCommandsFlow = MutableStateFlow(listOf<DomainCommand>())

    suspend fun storeCommands(domainCommands: List<DomainCommand>) {
        _unappliedCommands.addAll(domainCommands)
        unappliedCommandsFlow.value = unappliedCommands.toList()
    }

    private fun commandsForTemplate(templateId: ChecklistTemplateId): List<TemplateDomainCommand> {
        return unappliedCommands
            .filterIsInstance<TemplateDomainCommand>()
            .filter { command -> command.templateId == templateId }
    }

    fun commandsForChecklist(checklistId: ChecklistId): List<ChecklistDomainCommand> {
        return unappliedCommands
            .filterIsInstance<ChecklistDomainCommand>()
            .filter { command -> command.checklistId == checklistId }
    }

    fun commandOnlyTemplates(): List<ChecklistTemplate> {
        return unappliedCommands
            .filterIsInstance<TemplateDomainCommand>()
            .groupBy { it.templateId }
            .filterValues {
                it.any { command ->
                    command is TemplateDomainCommand.CreateNewTemplate
                }
            }
            .map { (id, commands) ->
                commands.fold(ChecklistTemplate.empty(id)) { acc, command ->
                    command.applyTo(acc)
                }
            }
    }

    fun rehydrate(template: ChecklistTemplate): ChecklistTemplate {
        return commandsForTemplate(template.id)
            .fold(template) { acc, command ->
                command.applyTo(acc)
            }
    }

    fun rehydrate(checklist: Checklist): Checklist {
        return commandsForChecklist(checklist.id)
            .fold(checklist) { acc, command ->
                command.applyTo(acc)
            }
    }

    suspend fun deleteCommands(ids: List<UUID>) {
        _unappliedCommands.removeIf { ids.contains(it.commandId) }
        unappliedCommandsFlow.emit(_unappliedCommands)
    }
}
