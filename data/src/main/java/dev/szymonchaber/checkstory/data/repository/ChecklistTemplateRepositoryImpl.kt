package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistTemplateRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.template.*
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistTemplateRepositoryImpl @Inject constructor(
    private val dataSource: ChecklistTemplateRoomDataSource
) : ChecklistTemplateRepository {

    override fun getAll(): Flow<List<ChecklistTemplate>> {
        return dataSource.getAll()
    }

    override fun get(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate> {
        return dataSource.getById(checklistTemplateId.id)
    }

    override fun create(): Flow<ChecklistTemplate> {
        return flow {
            val newChecklistTemplate = ChecklistTemplate(
                ChecklistTemplateId(0),
                "New checklist template",
                "Checklist description",
                listOf(
                    TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 1"),
                    TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 2")
                ),
                LocalDateTime.now()
            )
            emit(dataSource.insert(newChecklistTemplate))
        }
            .flowOn(Dispatchers.IO)
            .flatMapLatest {
                get(ChecklistTemplateId(it))
            }
    }

    override fun update(checklistTemplate: ChecklistTemplate): Flow<Unit> {
        return flow {
            dataSource.update(checklistTemplate)
            emit(Unit)
        }
    }

    override suspend fun delete(checklistTemplate: ChecklistTemplate) {
        dataSource.delete(checklistTemplate)
    }

    companion object {

        val templates = listOf(
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
    }
}
