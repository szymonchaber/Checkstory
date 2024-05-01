package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import kotlinx.datetime.Clock
import javax.inject.Inject

class DeleteTemplateUseCase @Inject constructor(
    private val storeCommandsUseCase: StoreCommandsUseCase
) {

    suspend fun deleteTemplate(templateId: TemplateId) {
        val deleteCommand = TemplateCommand.DeleteTemplate(templateId, Clock.System.now())
        storeCommandsUseCase.storeCommands(listOf(deleteCommand))
    }
}
