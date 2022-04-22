package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val checkboxes = checklist.items.flatMap {
            listOf(it) + it.children
        }.map(CheckboxEntity.Companion::fromDomainCheckbox)
        checkboxDao.insertAll(*checkboxes.toTypedArray())

        checklistDao.update(ChecklistEntity.fromDomainChecklist(checklist))
    }

    suspend fun insert(checklist: Checklist): Long {
        val checklistId = checklistDao.insert(ChecklistEntity.fromDomainChecklist(checklist))
        insertCheckboxes(checklist.items, checklistId)
        return checklistId
    }

    private suspend fun insertCheckboxes(checkboxes: List<Checkbox>, checklistId: Long) {
        checkboxes.forEach {
            coroutineScope {
                withContext(Dispatchers.Default) {
                    launch {
                        val parentId =
                            checkboxDao.insert(CheckboxEntity.fromDomainCheckbox(it).copy(checklistId = checklistId))
                        val children = it.children.map { child ->
                            CheckboxEntity.fromDomainCheckbox(child)
                                .copy(parentId = parentId, checklistId = checklistId)
                        }
                        checkboxDao.insertAll(*children.toTypedArray())
                    }
                }
            }
        }
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
                    toRelation(checkboxes)
                )
            }
    }

    private fun toRelation(checkboxes: List<CheckboxEntity>): List<Checkbox> {
        val parentToChildren: Map<Long?, List<CheckboxEntity>> = checkboxes.groupBy {
            it.parentId
        }
        return parentToChildren[null]?.map { parent ->
            parent.toDomainCheckbox(parentToChildren[parent.checkboxId]?.map {
                it.toDomainCheckbox(listOf())
            }.orEmpty())
        }.orEmpty()
    }

    suspend fun delete(checklist: Checklist) {
        val checkboxEntities = checklist.items.map(CheckboxEntity::fromDomainCheckbox)
        checkboxDao.delete(*checkboxEntities.toTypedArray())
        checklistDao.delete(ChecklistEntity.fromDomainChecklist(checklist))
    }
}
