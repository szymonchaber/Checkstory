package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {

    val checklistSavedEventFlow: Flow<ChecklistSaved>

    suspend fun save(checklist: Checklist)

    suspend fun get(checklistId: ChecklistId): Checklist?

    suspend fun deleteAllData()

    fun getCount(): Flow<Int>
}

object ChecklistSaved
