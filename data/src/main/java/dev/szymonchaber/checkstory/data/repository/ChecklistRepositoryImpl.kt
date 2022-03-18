package dev.szymonchaber.checkstory.data.repository

import dagger.hilt.android.scopes.ViewModelScoped
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ChecklistRepositoryImpl @Inject constructor() : ChecklistRepository {

    override fun getChecklist(checklistId: String): Flow<Checklist> {
        return flowOf(checklist)
            .onEach {
                delay(2000)
            }
            .flowOn(Dispatchers.IO)
    }
}
