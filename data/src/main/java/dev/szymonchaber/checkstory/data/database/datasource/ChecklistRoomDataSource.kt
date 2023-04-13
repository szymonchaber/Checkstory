package dev.szymonchaber.checkstory.data.database.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ChecklistRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val checklistDao: ChecklistDao,
    private val checkboxDao: CheckboxDao
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getById(id: Long): Flow<Checklist> {
        return checklistDao.getById(id)
            .filterNotNull()
            .flatMapLatest(::combineIntoDomainChecklist)
            .filterNotNull()
            .take(1)
    }

    fun getAll(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .toDomainChecklistFlow()
    }

    private fun getCheckboxes(checklistId: Long) = checklistDao.getCheckboxesForChecklist(checklistId)

    suspend fun insert(checklist: Checklist): Long {
        val checklistId = checklistDao.insert(ChecklistEntity.fromDomainChecklist(checklist))
        insertCheckboxes(checklist.items, checklistId)
        return checklistId
    }

    private suspend fun insertCheckboxes(checkboxes: List<Checkbox>, checklistId: Long) {
        checkboxes.forEach {
            withContext(Dispatchers.Default) {
                launch {
                    insertCheckboxRecursive(it, checklistId, null)
                }
            }
        }
    }

    private suspend fun insertCheckboxRecursive(
        checkbox: Checkbox,
        checklistId: Long,
        parentId: CheckboxId?
    ) {
        val nestedParentId = checkbox.id
        checkboxDao.insert(
            CheckboxEntity.fromDomainCheckbox(checkbox).copy(parentId = parentId?.id, checklistId = checklistId)
        )
        checkbox.children.forEach { child ->
            insertCheckboxRecursive(child, checklistId, nestedParentId)
        }
    }

    fun getBasedOn(basedOn: ChecklistTemplateId): Flow<List<Checklist>> {
        val all = checklistDao.getAll(basedOn.id)
        return all.toDomainChecklistFlow()
    }

    private fun Flow<List<ChecklistEntity>>.toDomainChecklistFlow(): Flow<List<Checklist>> {
        return flatMapLatest {
            it.map(::combineIntoDomainChecklist)
                .toFlowOfLists()
                .map {
                    it.filterNotNull()
                }
        }
    }

    private fun combineIntoDomainChecklist(checklist: ChecklistEntity): Flow<Checklist?> {
        return checklistTemplateDao.getById(checklist.templateId)
            .onEach {
                if (it == null) {
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception("Found orphaned checklist (no related checklist template found). Cascading deletion seems to be broken"))
                    Timber.e("Found orphaned checklist (no related checklist template found). Cascading deletion seems to be broken")
                }
            }
            .combine(getCheckboxes(checklist.checklistId)) { template, checkboxes ->
                template?.let {
                    checklist.toDomainChecklist(
                        it.title,
                        template.description,
                        groupToDomain(checkboxes)
                    )
                }
            }
    }

    private fun groupToDomain(checkboxes: List<CheckboxEntity>): List<Checkbox> {
        return convertToNestedCheckboxes(checkboxes)
    }

    private fun convertToNestedCheckboxes(entities: List<CheckboxEntity>): List<Checkbox> {
        val entityMap = entities.associateBy { it.checkboxId }
        val checkboxes = entities.map { CheckboxToChildren(it) }
        checkboxes.forEach { checkbox ->
            val parentId = checkbox.checkbox.parentId
            if (parentId != null) {
                val parent = entityMap[parentId]
                if (parent != null) {
                    val parentCheckbox = checkboxes.firstOrNull { it.checkbox.checkboxId == parent.checkboxId }
                    if (parentCheckbox != null) {
                        parentCheckbox.children += checkbox
                    }
                }
            }
        }
        return checkboxes.filter { it.checkbox.parentId == null }
            .map {
                toDomain(it)
            }
    }

    private fun toDomain(checkboxToChildren: CheckboxToChildren): Checkbox {
        return checkboxToChildren.checkbox.toDomainCheckbox(checkboxToChildren.children.map { toDomain(it) })
    }

    class CheckboxToChildren(
        val checkbox: CheckboxEntity,
        val children: MutableList<CheckboxToChildren> = mutableListOf()
    )

    suspend fun delete(checklist: Checklist) {
        val checkboxEntities = checklist.items.map(CheckboxEntity::fromDomainCheckbox)
        checkboxDao.delete(*checkboxEntities.toTypedArray())
        checklistDao.delete(ChecklistEntity.fromDomainChecklist(checklist))
    }
}
