package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import kotlinx.datetime.Clock
import java.util.*
import javax.inject.Inject

class DeleteChecklistUseCase @Inject constructor(
    private val storeCommandsUseCase: StoreCommandsUseCase
) {

    suspend fun deleteChecklist(checklistId: ChecklistId) {
        val deleteCommand = ChecklistCommand.DeleteChecklistCommand(checklistId, UUID.randomUUID(), Clock.System.now())
        storeCommandsUseCase.storeCommands(listOf(deleteCommand))
    }
}
