package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    val checklistSavedEvents: Flow<ChecklistSaved>

    suspend fun save(checklist: Checklist)

    suspend fun get(checklistId: ChecklistId): Checklist?

    fun getChecklist(checklistId: ChecklistId): Flow<Checklist>

    fun getAllChecklists(): Flow<List<Checklist>>

    suspend fun deleteAllData()
}

object ChecklistSaved
