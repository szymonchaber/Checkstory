package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.SynchronizationStatusRepository
import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import dev.szymonchaber.checkstory.domain.repository.TemplateRepository
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val synchronizer: Synchronizer,
    private val templateRepository: TemplateRepository,
    private val checklistRepository: ChecklistRepository,
    private val synchronizationStatusRepository: SynchronizationStatusRepository
) {

    suspend fun logoutSafely(): LogoutResult {
        return if (synchronizer.hasUnsynchronizedCommands()) {
            LogoutResult.UnsynchronizedCommandsPresent
        } else {
            performLogout()
            LogoutResult.Done
        }
    }

    suspend fun logoutIgnoringUnsynchronizedData() {
        performLogout()
    }

    private suspend fun performLogout() {
        synchronizer.deleteCommands()
        userRepository.removeCurrentUser()
        checklistRepository.deleteAllData()
        templateRepository.deleteAllData()
        synchronizationStatusRepository.clear()
    }
}
