package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistFactory
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistTemplateRepositoryImpl @Inject constructor() : ChecklistTemplateRepository {

    private val templateList = listOf(
        ChecklistFactory.createChecklistTemplate(
            "Cleaning living room",
            "I love to have a clean living room, but tend to forget about some hard-to-reach places",
            "Table",
            "Desk",
            "Floor",
            "Windows",
            "Couch",
            "Chairs",
            "Shelves",
        ),
        ChecklistFactory.createChecklistTemplate(
            "Cleaning kitchen",
            "Mise en place is my style, so let's make sure that kitchen is always ready to roll",
            "Wash the dishes",
            "Clean the worktop",
            "Clean the sink",
            "Clean cupboard fronts",
            "Arrange the pantry",
            "Clean windows",
            "Clean the floor",
            "Clear the fridge out",
            "Clean the induction cooker",
        )
    )

    private val templates = templateList.associateBy {
        it.id
    }

    override fun getAllChecklistTemplates(): Flow<List<ChecklistTemplate>> {
        return flowOf(
            templates.values.toList()
        )
            .onEach {
                delay(1000)
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getChecklistTemplate(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate> {
        return flowOf(
            templates.getValue(checklistTemplateId)
        )
            .onEach {
                delay(500)
            }
            .flowOn(Dispatchers.IO)
    }
}
