package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChecklistToFillUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {

    fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return checklistRepository.getChecklist(checklistId)
    }
}
