package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainCommand

interface Synchronizer {

    suspend fun synchronize()

    suspend fun synchronizeEvents(editTemplateDomainEvents: List<EditTemplateDomainCommand>)
}
