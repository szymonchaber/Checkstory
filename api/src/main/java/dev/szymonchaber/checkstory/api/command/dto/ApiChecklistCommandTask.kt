package dev.szymonchaber.checkstory.api.command.dto

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import kotlinx.serialization.Serializable

@Serializable
data class ApiChecklistCommandTask(
    val id: DtoUUID,
    val checklistId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val isChecked: Boolean,
    val children: List<ApiChecklistCommandTask> = listOf()
)
