package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.DeepChecklistEntity
import dev.szymonchaber.checkstory.data.database.dao.TaskDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistSaved
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChecklistRepositoryImpl @Inject constructor(
    private val checklistDao: ChecklistDao,
    private val taskDao: TaskDao,
    private val commandRepository: CommandRepository
) : ChecklistRepository {

    private val _checklistSavedEvents =
        MutableSharedFlow<ChecklistSaved>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val checklistSavedEvents: Flow<ChecklistSaved>
        get() = _checklistSavedEvents

    override suspend fun save(checklist: Checklist) {
        checklistDao.insert(ChecklistEntity.fromDomainChecklist(checklist))
        taskDao.insertAll(checklist.items.map(CheckboxEntity::fromDomainTask))
        _checklistSavedEvents.tryEmit(ChecklistSaved)
    }

    override fun getChecklist(checklistId: ChecklistId): Flow<Checklist> {
        return flow {
            emit(getByIdOrNull(checklistId.id))
        }
            .filterNotNull()
            .take(1)
    }

    override fun getAllChecklists(): Flow<List<Checklist>> {
        return checklistDao.getAll()
            .map {
                it.map {
                    toDomain(it)
                }
            }
            .hydrated()
    }

    override fun getChecklists(basedOn: TemplateId): Flow<List<Checklist>> {
        return getBasedOn(basedOn)
    }

    override suspend fun deleteBasedOnTemplate(template: Template) {
        getBasedOn(template.id)
            .first()
            .forEach {
                withContext(Dispatchers.IO) {
                    this@ChecklistRepositoryImpl.delete(it)
                }
            }
    }

    private suspend fun getByIdOrNull(id: UUID): Checklist? {
        return checklistDao.getByIdOrNull(id)?.let {
            toDomain(it)
        } ?: commandRepository.commandOnlyChecklists()
            .find { it.id.id == id }
    }

    private fun Flow<List<Checklist>>.hydrated(): Flow<List<Checklist>> =
        combine(commandRepository.getUnappliedCommandsFlow()) { templates, _ ->
            templates.map {
                commandRepository.hydrate(it)
            }
                .plus(commandRepository.commandOnlyChecklists())
                .distinctBy { it.id }
                .sortedByDescending(Checklist::createdAt)
                .filterNot(Checklist::isRemoved)
        }

    suspend fun insert(checklist: Checklist): ChecklistId {
        checklistDao.insert(ChecklistEntity.fromDomainChecklist(checklist))
        taskDao.insertAll(checklist.flattenedItems.map(CheckboxEntity::fromDomainTask))
        return checklist.id
    }

    fun getBasedOn(basedOn: TemplateId): Flow<List<Checklist>> {
        return checklistDao.getAllForTemplate(basedOn.id)
            .map {
                it.map { checklist ->
                    toDomain(checklist)
                }
            }
            .hydrated()
            .map { checklists ->
                checklists.filter { it.templateId == basedOn }
            }
    }

    private suspend fun toDomain(deepChecklistEntity: DeepChecklistEntity): Checklist {
        val (checklist, template, tasks) = deepChecklistEntity
        return checklist.toDomainChecklist(
            template.title,
            template.description,
            convertToNestedTasks(tasks)
        ).let {
            commandRepository.hydrate(it)
        }
    }

    private fun convertToNestedTasks(entities: List<CheckboxEntity>): List<Task> {
        val entityMap = entities.associateBy { it.checkboxId }
        val tasks = entities.map { TaskToChildren(it) }
        tasks.forEach { task ->
            val parentId = task.task.parentId
            if (parentId != null) {
                val parent = entityMap[parentId]
                if (parent != null) {
                    val parentTask = tasks.firstOrNull { it.task.checkboxId == parent.checkboxId }
                    if (parentTask != null) {
                        parentTask.children += task
                    }
                }
            }
        }
        return tasks.filter { it.task.parentId == null }
            .map {
                toDomain(it)
            }
    }

    private fun toDomain(taskToChildren: TaskToChildren): Task {
        return taskToChildren.task.toDomainTask(taskToChildren.children.map { toDomain(it) })
    }

    override suspend fun delete(checklist: Checklist) {
        val checkboxEntities = checklist.items.map(CheckboxEntity::fromDomainTask)
        taskDao.delete(*checkboxEntities.toTypedArray())
        checklistDao.delete(ChecklistEntity.fromDomainChecklist(checklist))
    }

    suspend fun replaceData(with: List<Checklist>) {
        val checklists = with.map(ChecklistEntity.Companion::fromDomainChecklist)
        val flatTasks = with.flatMap(Checklist::flattenedItems).map(CheckboxEntity.Companion::fromDomainTask)
        checklistDao.replaceData(checklists, flatTasks)
    }

    override suspend fun deleteAllData() {
        checklistDao.deleteAllData()
    }

    class TaskToChildren(
        val task: CheckboxEntity,
        val children: MutableList<TaskToChildren> = mutableListOf()
    )
}
