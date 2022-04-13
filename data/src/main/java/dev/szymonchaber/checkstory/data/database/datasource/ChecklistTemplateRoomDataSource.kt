package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class ChecklistTemplateRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val checkboxDao: TemplateCheckboxDao
) {

    fun getById(id: Long): Flow<ChecklistTemplate> {
        return checklistTemplateDao.getById(id)
            .flatMapLatest(::combineIntoDomainChecklistTemplate)
    }

    fun getAll(): Flow<List<ChecklistTemplate>> {
        return checklistTemplateDao.getAll()
            .flatMapLatest {
                it.map(::combineIntoDomainChecklistTemplate).toFlowOfLists()
            }
    }

    suspend fun update(checklistTemplate: ChecklistTemplate): Long {
        return insert(checklistTemplate)
    }

    suspend fun updateTemplateCheckbox(
        templateCheckbox: TemplateCheckbox,
        templateId: ChecklistTemplateId
    ) {
        checkboxDao.insert(
            TemplateCheckboxEntity.fromDomainTemplateCheckbox(
                templateCheckbox,
                templateId.id
            )
        )
    }

    suspend fun createTemplateCheckbox(
        templateCheckbox: TemplateCheckbox,
        templateId: ChecklistTemplateId
    ): Long {
        return checkboxDao.insert(
            TemplateCheckboxEntity.fromDomainTemplateCheckbox(templateCheckbox, templateId.id)
        )
    }

    suspend fun getTemplateCheckbox(id: TemplateCheckboxId): TemplateCheckbox {
        return toDomainTemplateCheckbox(checkboxDao.getById(id.id))
    }

    suspend fun insert(checklistTemplate: ChecklistTemplate): Long {
        val checklistTemplateId = checklistTemplateDao.insert(
            ChecklistTemplateEntity.fromDomainChecklistTemplate(checklistTemplate)
        )
        checkboxDao.insertAll(
            *checklistTemplate.items.map {
                TemplateCheckboxEntity.fromDomainTemplateCheckbox(
                    it,
                    checklistTemplateId
                )
            }.toTypedArray()
        )
        return checklistTemplateId
    }

    private fun combineIntoDomainChecklistTemplate(entity: ChecklistTemplateEntity): Flow<ChecklistTemplate> {
        return checkboxDao.getAllForChecklistTemplate(entity.id).map { checkboxes ->
            mapChecklistTemplate(entity, checkboxes)
        }
    }

    private fun mapChecklistTemplate(
        template: ChecklistTemplateEntity,
        checkboxes: List<TemplateCheckboxEntity>
    ): ChecklistTemplate {
        return with(template) {
            ChecklistTemplate(
                ChecklistTemplateId(id),
                title,
                description,
                checkboxes.map {
                    toDomainTemplateCheckbox(it)
                },
                LocalDateTime.now()
            )
        }
    }

    private fun toDomainTemplateCheckbox(templateCheckboxEntity: TemplateCheckboxEntity): TemplateCheckbox {
        return TemplateCheckbox(
            TemplateCheckboxId(templateCheckboxEntity.checkboxId),
            templateCheckboxEntity.checkboxTitle
        )
    }
}
