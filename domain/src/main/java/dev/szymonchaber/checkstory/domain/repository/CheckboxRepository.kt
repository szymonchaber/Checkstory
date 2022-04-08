package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox

interface CheckboxRepository {

    suspend fun updateCheckbox(checkbox: Checkbox)
}
