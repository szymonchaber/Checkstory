package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import javax.inject.Inject

class StoreCommandsUseCase @Inject constructor(
    private val synchronizer: Synchronizer,
) {

    suspend fun storeCommands(commands: List<Command>) {
        synchronizer.storeCommands(commands)
    }
}
