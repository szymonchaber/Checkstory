package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent

interface Synchronizer {

    suspend fun synchronize()

    suspend fun synchronizeEvents(editTemplateDomainEvents: List<EditTemplateDomainEvent>)
}
