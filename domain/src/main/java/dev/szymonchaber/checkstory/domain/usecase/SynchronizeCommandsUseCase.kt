package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import javax.inject.Inject

class SynchronizeCommandsUseCase @Inject constructor(
    private val synchronizer: Synchronizer,
) {

    suspend fun synchronizeCommands(templateDomainCommands: List<TemplateDomainCommand>) {
        synchronizer.synchronizeCommands(templateDomainCommands)
    }
}
