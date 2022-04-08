package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChecklistRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val checklistDao: ChecklistDao,
    private val checkboxDao: CheckboxDao
) {

    fun getById(id: Long): Flow<Checklist> {
        return checklistDao.getById(id)
            .map {
                it.map { (checklist, checkboxes) ->
                    val (_, title, description) = checklistTemplateDao.getById(checklist.templateId)
                        .first().keys.first()
                    checklist.toDomainChecklist(
                        title,
                        description,
                        checkboxes.map(CheckboxEntity::toDomainCheckbox)
                    )
                }.first()
            }
    }

    fun getAll(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .map {
                it.map { (checklist, checkboxes) ->
                    val (_, title, description) = checklistTemplateDao.getById(checklist.templateId)
                        .first().keys.first()
                    checklist.toDomainChecklist(
                        title,
                        description,
                        checkboxes.map(CheckboxEntity::toDomainCheckbox)
                    )
                }
            }
    }

    suspend fun update(checklist: Checklist) {
        return checklistDao.update(
            ChecklistEntity.fromDomainChecklist(checklist)
        )
    }

    suspend fun updateCheckbox(checkbox: Checkbox) {
        val checklistId =
            checkboxDao.getById(checkbox.id.id).checklistId // TODO could be baked into Checkbox
        checkboxDao.update(
            CheckboxEntity.fromDomainCheckbox(
                checkbox,
                checklistId
            )
        )
    }

    suspend fun insert(checklist: Checklist): Long {
        val checklistId = checklistDao.insert(
            ChecklistEntity.fromDomainChecklist(checklist)
        )
        checkboxDao.insertAll(
            *checklist.items.map {
                CheckboxEntity.fromDomainCheckbox(
                    it,
                    checklistId
                )
            }.toTypedArray()
        )
        return checklistId
    }
}
