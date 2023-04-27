package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand

interface Synchronizer {

    suspend fun synchronize()

    suspend fun synchronizeCommands(editTemplateDomainEvents: List<TemplateDomainCommand>)
}
