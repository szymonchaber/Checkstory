package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import javax.inject.Inject

class GetChecklistToFillUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {

    suspend fun getChecklist(checklistId: ChecklistId): Checklist? {
        return checklistRepository.get(checklistId)
    }
}
