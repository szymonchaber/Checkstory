package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    suspend fun save(checklist: Checklist)

    fun getChecklist(checklistId: ChecklistId): Flow<Checklist>

    fun getAllChecklists(): Flow<List<Checklist>>

    fun getChecklists(basedOn: ChecklistTemplateId): Flow<List<Checklist>>

    suspend fun delete(checklist: Checklist)

    suspend fun deleteBasedOnTemplate(checklistTemplate: ChecklistTemplate)
}
