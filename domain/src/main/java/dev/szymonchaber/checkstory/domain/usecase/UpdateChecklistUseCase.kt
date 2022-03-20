package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {

    fun updateChecklist(checklist: Checklist): Flow<Unit> {
        return checklistRepository.update(checklist)
    }
}
