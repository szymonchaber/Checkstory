package dev.szymonchaber.checkstory.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistTemplateDto(
    val id: Long,
    val title: String,
    val description: String,
    val templateCheckboxes: List<TemplateCheckboxDto>
//        val createdAt: Date?
)
