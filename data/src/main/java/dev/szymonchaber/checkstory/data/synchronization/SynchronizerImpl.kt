package dev.szymonchaber.checkstory.data.synchronization

import dev.szymonchaber.checkstory.data.Event
import dev.szymonchaber.checkstory.data.State
import dev.szymonchaber.checkstory.data.api.event.EventsApi
import dev.szymonchaber.checkstory.data.repository.LocalChecklistTemplateRepository
import dev.szymonchaber.checkstory.data.repository.RemoteChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynchronizerImpl @Inject internal constructor(
    private val checklistTemplateRepository: LocalChecklistTemplateRepository,
    private val remoteChecklistTemplateRepository: RemoteChecklistTemplateRepository,
    private val eventsApi: EventsApi
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
    }

    override suspend fun synchronizeEvents(editTemplateDomainEvents: List<EditTemplateDomainEvent>) {
        eventsApi.pushEvents(editTemplateDomainEvents)
    }
}
