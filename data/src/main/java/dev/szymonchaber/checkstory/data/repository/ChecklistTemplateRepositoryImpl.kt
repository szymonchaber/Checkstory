package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistTemplateRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.template.*
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistTemplateRepositoryImpl @Inject constructor(
    private val dataSource: ChecklistTemplateRoomDataSource
) : ChecklistTemplateRepository {

    private val templatesFlow = MutableStateFlow(templates.associateBy {
        it.id
    })

    override fun getAllChecklistTemplates(): Flow<List<ChecklistTemplate>> {
        return dataSource.getAll()
    }

    override fun getChecklistTemplate(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate> {
        return dataSource.getById(checklistTemplateId.id.toLong())
    }

    override fun createChecklistTemplate(): Flow<ChecklistTemplate> {
        return flow {
            val newChecklistTemplate = ChecklistTemplate(
                ChecklistTemplateId(UUID.randomUUID().toString()),
                "New title",
                "New description",
                listOf(
                    TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 1"),
                    TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 2")
                )
            )
            templatesFlow.update {
                it.plus(newChecklistTemplate.id to newChecklistTemplate)
            }
            emit(newChecklistTemplate.id)
        }
            .flowOn(Dispatchers.IO)
            .flatMapLatest {
                getChecklistTemplate(it)
            }
    }

    override fun updateChecklistTemplate(checklistTemplate: ChecklistTemplate): Flow<Unit> {
        return flow {
//            templatesFlow.update {
//                it.plus(checklistTemplate.id to checklistTemplate)
//            }
            dataSource.update(checklistTemplate)
            emit(Unit)
        }
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
