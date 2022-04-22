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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class ChecklistTemplateRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val templateCheckboxDao: TemplateCheckboxDao
) {

    fun getById(id: Long): Flow<ChecklistTemplate> {
        return checklistTemplateDao.getById(id)
            .filterNotNull()
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

    suspend fun insert(checklistTemplate: ChecklistTemplate): Long {
        val checklistTemplateId = checklistTemplateDao.insert(
            ChecklistTemplateEntity.fromDomainChecklistTemplate(checklistTemplate)
        )
        templateCheckboxDao.insertAll(
            // TODO how does it know its parent id?
            *flattenWithChildren(checklistTemplate)
                .map {
                    TemplateCheckboxEntity.fromDomainTemplateCheckbox(
                        it,
                        checklistTemplateId
                    )
                }
                .toTypedArray()
        )
        return checklistTemplateId
    }

    private fun flattenWithChildren(checklistTemplate: ChecklistTemplate): List<TemplateCheckbox> {
        return checklistTemplate.items.flatMap {
            listOf(it).plus(it.children)
        }
    }

    private fun combineIntoDomainChecklistTemplate(entity: ChecklistTemplateEntity): Flow<ChecklistTemplate> {
        return templateCheckboxDao.getAllForChecklistTemplate(entity.id).map { checkboxes ->
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
                groupToDomain(checkboxes),
                LocalDateTime.now()
            )
        }
    }

    private fun groupToDomain(checkboxes: List<TemplateCheckboxEntity>): List<TemplateCheckbox> {
        val parentToChildren: Map<Long?, List<TemplateCheckboxEntity>> = checkboxes.groupBy {
            it.parentId
        }
        return parentToChildren[null]?.map { parent ->
            toDomainTemplateCheckbox(parent, null, parentToChildren[parent.checkboxId]?.map {
                toDomainTemplateCheckbox(it, TemplateCheckboxId(parent.checkboxId), listOf())
            }.orEmpty())
        }.orEmpty()
    }

    private fun toDomainTemplateCheckbox(
        templateCheckboxEntity: TemplateCheckboxEntity,
        parentId: TemplateCheckboxId?,
        children: List<TemplateCheckbox>
    ): TemplateCheckbox {
        return TemplateCheckbox(
            TemplateCheckboxId(templateCheckboxEntity.checkboxId),
            parentId,
            templateCheckboxEntity.checkboxTitle,
            children
        )
    }

    suspend fun delete(checklistTemplate: ChecklistTemplate) {
        checklistTemplateDao.delete(ChecklistTemplateEntity.fromDomainChecklistTemplate(checklistTemplate))
    }

    suspend fun deleteCheckboxesFromTemplate(checklistTemplate: ChecklistTemplate) {
        templateCheckboxDao.deleteAllFromTemplate(checklistTemplate.id.id)
    }

    suspend fun deleteTemplateCheckbox(templateCheckbox: TemplateCheckbox) {
        templateCheckboxDao.delete(templateCheckbox.id.id)
    }
}
