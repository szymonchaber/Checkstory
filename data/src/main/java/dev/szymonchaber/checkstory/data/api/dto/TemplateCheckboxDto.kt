package dev.szymonchaber.checkstory.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class TemplateCheckboxDto(
    val id: String,
    val title: String,
    val templateId: Long,
)
