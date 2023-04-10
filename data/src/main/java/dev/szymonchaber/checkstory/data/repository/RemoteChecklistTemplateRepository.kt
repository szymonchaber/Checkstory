package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.api.ChecklistTemplateApi
import dev.szymonchaber.checkstory.data.api.dto.ChecklistTemplateDto
import dev.szymonchaber.checkstory.data.api.dto.TemplateCheckboxDto
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemoteChecklistTemplateRepository @Inject constructor(
    private val checklistTemplateApi: ChecklistTemplateApi
) : ChecklistTemplateRepository {

    override fun getAll(): Flow<List<ChecklistTemplate>> {
        return flow {
            emit(checklistTemplateApi.getAllChecklistTemplates().map {
                it.toChecklistTemplate()
            })
        }
    }

    private fun ChecklistTemplateDto.toChecklistTemplate(): ChecklistTemplate {
        return ChecklistTemplate(
            ChecklistTemplateId(id),
            title,
            description,
            items = templateCheckboxes.map {
                TemplateCheckbox(
                    TemplateCheckboxId(it.id),
                    null,
                    it.title,
                    listOf(),
                    0 // TODO
                )
            },
            createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
            listOf(),
            listOf()
        )
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

    override suspend fun helloWorld(token: String): String {
        return checklistTemplateApi.helloWorld(token)
    }

    suspend fun pushAll(checklistTemplates: List<ChecklistTemplate>): List<ChecklistTemplate> {
        val payload = checklistTemplates.map {
            ChecklistTemplateDto(
                0,
                it.title,
                it.description,
                it.items.map {
                    TemplateCheckboxDto(
                        0,
                        it.title,
                        0
                    )
                },
                it.createdAt.toInstant(ZoneOffset.UTC).toKotlinInstant()
            )
        }
        return checklistTemplateApi.pushChecklistTemplates(payload).map {
            it.toChecklistTemplate()
        }
    }
}
