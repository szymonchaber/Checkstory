package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {

    fun getAll(): Flow<List<Template>>

    suspend fun get(templateId: TemplateId): Template?

    suspend fun save(template: Template)

    suspend fun delete(template: Template)

    suspend fun deleteAllData()

    suspend fun replaceData(with: List<Template>)
}
