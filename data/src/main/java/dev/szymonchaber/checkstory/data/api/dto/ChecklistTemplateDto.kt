package dev.szymonchaber.checkstory.data.api.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChecklistTemplateDto(
    val id: String,
    val title: String,
    val description: String,
    val templateCheckboxes: List<TemplateCheckboxDto>,
    val createdAt: Instant
)
