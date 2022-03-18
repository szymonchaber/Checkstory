package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    fun getChecklist(checklistId: String): Flow<Checklist>
}
