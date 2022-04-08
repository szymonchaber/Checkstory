package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.ChecklistTemplateRoomDataSource
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.repository.TemplateCheckboxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateCheckboxRepositoryImpl @Inject constructor(
    private val dataSource: ChecklistTemplateRoomDataSource
) : TemplateCheckboxRepository {

    override fun updateTemplateCheckbox(
        templateCheckbox: TemplateCheckbox,
        templateId: ChecklistTemplateId
    ): Flow<Unit> {
        return flow {
            dataSource.updateTemplateCheckbox(templateCheckbox, templateId)
            emit(Unit)
        }
    }

    override fun createTemplateCheckbox(templateId: ChecklistTemplateId): Flow<TemplateCheckbox> {
        return flow {
            emit(
                dataSource.createTemplateCheckbox(
                    TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 1"), templateId
                )
            )
        }
            .flowOn(Dispatchers.IO)
            .map {
                dataSource.getTemplateCheckbox(TemplateCheckboxId(it))
            }
    }
}
