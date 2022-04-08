package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.repository.CheckboxRepository
import javax.inject.Inject

class CheckboxRepositoryImpl @Inject constructor(
    val dataSource: ChecklistRoomDataSource
) : CheckboxRepository {

    override suspend fun updateCheckbox(checkbox: Checkbox) {
        dataSource.updateCheckbox(checkbox)
    }
}
