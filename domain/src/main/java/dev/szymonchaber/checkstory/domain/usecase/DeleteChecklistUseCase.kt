package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import javax.inject.Inject

class DeleteChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {

    suspend fun deleteChecklist(checklist: Checklist) {
        return checklistRepository.delete(checklist)
    }
}
