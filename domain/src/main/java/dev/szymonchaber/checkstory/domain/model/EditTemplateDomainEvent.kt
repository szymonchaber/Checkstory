package dev.szymonchaber.checkstory.domain.model

sealed interface EditTemplateDomainEvent {

    val timestamp: Long

    data class CreateNewTemplate(val id: String, override val timestamp: Long) : EditTemplateDomainEvent

    data class RenameTemplate(val id: String, val newTitle: String, override val timestamp: Long) :
        EditTemplateDomainEvent
}
