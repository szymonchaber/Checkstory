package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChecklistTemplateRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao
) {

    fun getAllChecklistTemplates(): Flow<List<ChecklistTemplate>> {
        return checklistTemplateDao.getAll()
            .map {
                it.map(::mapChecklistTemplate)
            }
    }

    private fun mapChecklistTemplate(checklistTemplateEntity: ChecklistTemplateEntity): ChecklistTemplate {
        return with(checklistTemplateEntity) {
            ChecklistTemplate(
                ChecklistTemplateId(id.toString()),
                title,
                description,
                listOf()
            )
        }
    }
}