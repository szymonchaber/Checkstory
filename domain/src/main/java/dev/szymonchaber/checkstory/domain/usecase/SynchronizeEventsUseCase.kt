package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import javax.inject.Inject

class SynchronizeEventsUseCase @Inject constructor(
    val synchronizer: Synchronizer,
) {

    fun synchronizeEvents(editTemplateDomainEvents: List<EditTemplateDomainEvent>) {

    }
}
