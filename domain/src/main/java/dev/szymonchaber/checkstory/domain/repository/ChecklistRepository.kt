package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    fun createAndGet(basedOn: ChecklistTemplate): Flow<Checklist>

    fun update(checklist: Checklist): Flow<Unit>

    fun getChecklist(checklistId: ChecklistId): Flow<Checklist>

    fun getAllChecklists(): Flow<List<Checklist>>
}
