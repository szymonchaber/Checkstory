package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChecklistTemplateRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val checkboxDao: TemplateCheckboxDao
) {

    fun getById(id: Long): Flow<ChecklistTemplate> {
        return checklistTemplateDao.getAll()
            .map {
                it.filterKeys {
                    it.id == id // why not direct?
                }
            }
            .map {
                it.map {
                    it.toChecklistTemplate()
                }.first()
            }
//        return checklistTemplateDao.getById(id)
//            .map {
//                it.toChecklistTemplate()
//                    .first()
//            }
    }

    fun getAll(): Flow<List<ChecklistTemplate>> {
        return checklistTemplateDao.getAll()
            .map {
                it.map {
                    it.toChecklistTemplate()
                }
            }
    }

    suspend fun update(checklistTemplate: ChecklistTemplate): Long {
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

    private fun Map.Entry<ChecklistTemplateEntity, List<TemplateCheckboxEntity>>.toChecklistTemplate(): ChecklistTemplate {
        val (template, checkboxes) = this
        return mapChecklistTemplate(template, checkboxes)
    }

    private fun mapChecklistTemplate(
        template: ChecklistTemplateEntity,
        checkboxes: List<TemplateCheckboxEntity>
    ): ChecklistTemplate {
        return with(template) {
            ChecklistTemplate(
                ChecklistTemplateId(id.toString()),
                title,
                description,
                checkboxes.map {
                    TemplateCheckbox(TemplateCheckboxId(it.checkboxId), it.checkboxTitle)
                }
            )
        }
    }
}
