package dev.szymonchaber.checkstory.data.database.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.toFlowOfLists
import dev.szymonchaber.checkstory.data.repository.CommandRepositoryImpl
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ChecklistRoomDataSource @Inject constructor(
    private val checklistTemplateDao: ChecklistTemplateDao,
    private val checklistDao: ChecklistDao,
    private val checkboxDao: CheckboxDao,
    private val commandRepository: CommandRepositoryImpl
) {

    fun getById(id: UUID): Flow<Checklist> {
        return flow {
            emit(getByIdOrNull(id))
        }
            .filterNotNull()
            .take(1)
    }

    private suspend fun getByIdOrNull(id: UUID): Checklist? {
        return checklistDao.getByIdOrNull(id)?.let {
            combineIntoDomainChecklist(it)
        }?.firstOrNull()
            ?: commandRepository.commandOnlyChecklists()
                .find { it.id.id == id }
    }

    fun getAll(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .toDomainChecklistFlow()
            .hydrated()
    }

    private fun Flow<List<Checklist>>.hydrated(): Flow<List<Checklist>> =
        combine(commandRepository.unappliedCommandsFlow) { templates, _ ->
            templates.map {
                commandRepository.hydrate(it)
            }
                .plus(commandRepository.commandOnlyChecklists())
                .sortedByDescending(Checklist::createdAt)
                .filterNot(Checklist::isRemoved)
        }

    private fun getCheckboxes(checklistId: UUID) = checklistDao.getCheckboxesForChecklist(checklistId)

    suspend fun insert(checklist: Checklist): ChecklistId {
        checklistDao.insert(ChecklistEntity.fromDomainChecklist(checklist))
        insertCheckboxes(checklist.items, checklist.id)
        return checklist.id
    }

    private suspend fun insertCheckboxes(checkboxes: List<Checkbox>, checklistId: ChecklistId) {
        checkboxes.forEach {
            withContext(Dispatchers.Default) {
                launch {
                    insertCheckboxRecursive(it, checklistId.id, null)
                }
            }
        }
    }

    private suspend fun insertCheckboxRecursive(
        checkbox: Checkbox,
        checklistId: UUID,
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
            .hydrated()
            .map { checklists ->
                checklists.filter { it.checklistTemplateId == basedOn }
            }
    }

    private fun Flow<List<ChecklistEntity>>.toDomainChecklistFlow(): Flow<List<Checklist>> {
        return flatMapLatest {
            it.map(::combineIntoDomainChecklist)
                .toFlowOfLists()
                .map(List<Checklist?>::filterNotNull)
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
            .map {
                it?.let {
                    commandRepository.hydrate(it)
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

    suspend fun insertAll(checklists: List<Checklist>) {
        withContext(Dispatchers.Default) {
            awaitAll(
                *checklists.map {
                    async {
                        insert(it)
                    }
                }.toTypedArray()
            )
        }
    }

    suspend fun deleteAll() {
        getAll().first().let {
            it.forEach {
                delete(it)
            }
        }
    }

}
