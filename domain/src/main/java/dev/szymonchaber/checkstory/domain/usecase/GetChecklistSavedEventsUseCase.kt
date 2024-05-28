package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import javax.inject.Inject

class GetChecklistSavedEventsUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {

    fun getChecklistSavedEvents() = checklistRepository.checklistSavedEventFlow
}
