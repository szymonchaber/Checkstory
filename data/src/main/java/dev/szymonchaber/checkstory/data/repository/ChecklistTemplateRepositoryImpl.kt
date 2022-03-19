package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.checklistTemplate
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ChecklistTemplateRepositoryImpl @Inject constructor() : ChecklistTemplateRepository {

    override fun getAllChecklistTemplates(): Flow<List<ChecklistTemplate>> {
        return flowOf(
            listOf(
                checklistTemplate,
                checklistTemplate.copy(
                    title = "Cleaning bedroom",
                    description = "I like my bedroom clean, too!"
                )
            )
        )
            .onEach {
                delay(2000)
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getChecklistTemplate(checklistTemplateId: String): Flow<ChecklistTemplate> {
        return flowOf(checklistTemplate)
            .onEach {
                delay(2000)
            }
            .flowOn(Dispatchers.IO)
    }
}
