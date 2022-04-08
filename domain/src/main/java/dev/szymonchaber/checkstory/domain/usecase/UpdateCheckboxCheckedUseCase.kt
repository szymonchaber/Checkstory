package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.repository.CheckboxRepository
import javax.inject.Inject

class UpdateCheckboxUseCase @Inject constructor(
    private val checkboxRepository: CheckboxRepository
) {

    suspend fun updateCheckbox(checkbox: Checkbox) {
        return checkboxRepository.updateCheckbox(checkbox)
    }
}
