package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.DomainCommand

interface Synchronizer {

    suspend fun hasUnsynchronizedCommands(): Boolean

    suspend fun synchronize()

    suspend fun synchronizeCommands(commands: List<DomainCommand>)

    suspend fun deleteCommands()
}
