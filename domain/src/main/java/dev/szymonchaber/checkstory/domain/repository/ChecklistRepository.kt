package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    fun createAndGet(basedOn: ChecklistTemplate): Flow<Checklist>

    fun getChecklist(checklistId: String): Flow<Checklist>
}
