package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ChecklistRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val checklistDao: ChecklistDao,
    private val checkboxDao: CheckboxDao
) {

    fun getById(id: Long): Flow<Checklist> {
        return checklistDao.getById(id)
            .flatMapLatest {
                it.map { (checklist, checkboxes) ->
                    checklistTemplateDao.getById(checklist.templateId).map {
                        val (_, title, description) = it.keys.first()
                        checklist.toDomainChecklist(
                            title,
                            description,
                            checkboxes.map(CheckboxEntity::toDomainCheckbox)
                        )
                    }
                }.first()
            }
    }

    fun getAll(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .toDomainChecklistFlow()
    }

    private fun getTemplate(templateId: Long) = checklistTemplateDao.getSingleById(templateId)

    private fun getCheckboxes(checklistId: Long) = checklistDao.getCheckboxesForChecklist(checklistId)

    suspend fun update(checklist: Checklist) {
        return checklistDao.update(
            ChecklistEntity.fromDomainChecklist(checklist)
        )
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
            val checklists: List<Flow<Checklist>> = it.map { checklist ->
                getTemplate(checklist.templateId).combine(getCheckboxes(checklist.checklistId)) { template, checkboxes ->
                    checklist.toDomainChecklist(
                        template.title,
                        template.description,
                        checkboxes.map(CheckboxEntity::toDomainCheckbox)
                    )
                }
            }
            checklists.fold(flowOf(listOf())) { flow, checklistFlow ->
                flow.combine(checklistFlow) { left, right ->
                    left.plus(right)
                }
            }
        }
    }
}
