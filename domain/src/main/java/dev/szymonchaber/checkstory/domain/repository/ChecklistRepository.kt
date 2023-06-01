package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    val checklistSavedEvents: Flow<ChecklistSaved>

    suspend fun save(checklist: Checklist)

    fun getChecklist(checklistId: ChecklistId): Flow<Checklist>

    fun getAllChecklists(): Flow<List<Checklist>>

    fun getChecklists(basedOn: TemplateId): Flow<List<Checklist>>

    suspend fun delete(checklist: Checklist)

    suspend fun deleteBasedOnTemplate(template: Template)

    suspend fun deleteAllData()
}

object ChecklistSaved
