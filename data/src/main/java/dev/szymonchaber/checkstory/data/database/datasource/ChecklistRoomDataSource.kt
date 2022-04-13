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
            .flatMapLatest {
                val checklists = it.map { (checklist, checkboxes) ->
                    checklistTemplateDao.getById(checklist.templateId)
                        .map {
                            val (_, title, description) = it.keys.first()
                            listOf(
                                checklist.toDomainChecklist(
                                    title,
                                    description,
                                    checkboxes.map(CheckboxEntity::toDomainCheckbox)
                                )
                            )
                        }
                }
                if (checklists.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    checklists.reduce { acc, checklistsFlow ->
                        acc.combine(checklistsFlow) { left, right ->
                            left + right
                        }
                    }
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

    fun getBasedOn(basedOn: ChecklistTemplateId): Flow<List<Checklist>> {
        return checklistDao.getAll(basedOn.id)
            .flatMapLatest {
                val map: List<Flow<List<Checklist>>> = it.map { (checklist, checkboxes) ->
                    checklistTemplateDao.getById(checklist.templateId)
                        .map {
                            val (_, title, description) = it.keys.first()
                            listOf(
                                checklist.toDomainChecklist(
                                    title,
                                    description,
                                    checkboxes.map(CheckboxEntity::toDomainCheckbox)
                                )
                            )
                        }
                }
                val reduce: Flow<List<Checklist>> = map.reduce { left, right ->
                    left.combine(right) { left, right ->
                        left + right
                    }
                }
                reduce
            }
    }
}
