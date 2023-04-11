package dev.szymonchaber.checkstory.data.synchronization

import dev.szymonchaber.checkstory.data.Event
import dev.szymonchaber.checkstory.data.State
import dev.szymonchaber.checkstory.data.repository.LocalChecklistTemplateRepository
import dev.szymonchaber.checkstory.data.repository.RemoteChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynchronizerImpl @Inject internal constructor(
    private val checklistTemplateRepository: LocalChecklistTemplateRepository,
    private val remoteChecklistTemplateRepository: RemoteChecklistTemplateRepository
) : Synchronizer {

    private val _events = mutableListOf<Event>()
    val events: List<Event>
        get() = _events

    fun checklistTitleChanged(id: String, newTitle: String) {
        _events.add(Event.TemplateTitleChanged(id, newTitle))
    }

    fun checklistTemplateTitleChanged() {

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
    // push all to backend
    // push all changes to backend
    // get all data from backend - updating if it's already there

    override suspend fun synchronize() {
        val checklistTemplates = checklistTemplateRepository.getAll().first()
        val checklistTemplatesWithRemoteId = remoteChecklistTemplateRepository.pushAll(checklistTemplates)
        Timber.d("Response: $checklistTemplatesWithRemoteId")
//        checklistTemplateRepository.updateAll(checklistTemplatesWithRemoteId)
    }
}
