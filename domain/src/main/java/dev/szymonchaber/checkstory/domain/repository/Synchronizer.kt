package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.Command

interface Synchronizer {

    suspend fun hasUnsynchronizedCommands(): Boolean

    suspend fun scheduleSynchronization()

    suspend fun synchronizeCommands(commands: List<Command>)

    suspend fun deleteCommands()
}

sealed interface SynchronizationResult {

    object Success : SynchronizationResult

    object Error : SynchronizationResult
}
