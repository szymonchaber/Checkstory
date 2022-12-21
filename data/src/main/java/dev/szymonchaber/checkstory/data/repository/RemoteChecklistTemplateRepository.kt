package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.api.ChecklistTemplateApi
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemoteChecklistTemplateRepository @Inject constructor(
    private val checklistTemplateApi: ChecklistTemplateApi
) : ChecklistTemplateRepository {

    override fun getAll(): Flow<List<ChecklistTemplate>> {
        return flow {
            emit(checklistTemplateApi.getAllChecklistTemplates().map {
                ChecklistTemplate(
                    ChecklistTemplateId(it.id),
                    it.title,
                    it.description,
                    items = it.templateCheckboxes.map {
                        TemplateCheckbox(
                            TemplateCheckboxId(it.id),
                            null,
                            it.title,
                            listOf()
                        )
                    },
                    it.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
                    listOf(),
                    listOf()
                )
            })
        }
    }

    override fun get(checklistTemplateId: ChecklistTemplateId): Flow<ChecklistTemplate> {
        TODO()
    }

    override suspend fun getOrNull(checklistTemplateId: ChecklistTemplateId): ChecklistTemplate? {
        TODO()
    }

    override suspend fun update(checklistTemplate: ChecklistTemplate) {
        TODO()
    }

    override suspend fun delete(checklistTemplate: ChecklistTemplate) {
        TODO()
    }

}
