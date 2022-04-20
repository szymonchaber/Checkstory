package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistTemplateRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
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
                    TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 1", listOf()),
                    TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 2", listOf())
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
}
