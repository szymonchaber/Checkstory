package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class ChecklistRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val checklistDao: ChecklistDao,
    private val checkboxDao: CheckboxDao
) {

    fun getById(id: Long): Flow<Checklist> {
        return checklistDao.getById(id)
            .filterNotNull()
            .flatMapLatest(::combineIntoDomainChecklist)
    }

    fun getAll(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .toDomainChecklistFlow()
    }

    private fun getCheckboxes(checklistId: Long) = checklistDao.getCheckboxesForChecklist(checklistId)

    suspend fun update(checklist: Checklist) {
        return checklistDao.update(ChecklistEntity.fromDomainChecklist(checklist))
    }

    suspend fun updateCheckbox(checkbox: Checkbox) {
        val checklistId = checkboxDao.getById(checkbox.id.id).checklistId // TODO could be baked into Checkbox
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

    fun getBasedOn(basedOn: ChecklistTemplateId): Flow<List<Checklist>> {
        val all = checklistDao.getAll(basedOn.id)
        return all.toDomainChecklistFlow()
    }

    private fun Flow<List<ChecklistEntity>>.toDomainChecklistFlow(): Flow<List<Checklist>> {
        return flatMapLatest {
            it.map(::combineIntoDomainChecklist).toFlowOfLists()
        }
    }

    private fun combineIntoDomainChecklist(checklist: ChecklistEntity): Flow<Checklist> {
        return checklistTemplateDao.getById(checklist.templateId)
            .filterNotNull()
            .combine(getCheckboxes(checklist.checklistId)) { template, checkboxes ->
                checklist.toDomainChecklist(
                    template.title,
                    template.description,
                    checkboxes.map(CheckboxEntity::toDomainCheckbox)
                )
            }
    }

    suspend fun delete(checklist: Checklist) {
        val checkboxEntities = checklist.items.map {
            CheckboxEntity.fromDomainCheckbox(it, checklist.id.id)
        }
        checkboxDao.delete(*checkboxEntities.toTypedArray())
        checklistDao.delete(ChecklistEntity.fromDomainChecklist(checklist))
    }
}
