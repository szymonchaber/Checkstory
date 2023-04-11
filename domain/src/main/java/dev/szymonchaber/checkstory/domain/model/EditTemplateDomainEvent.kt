package dev.szymonchaber.checkstory.domain.model

sealed interface EditTemplateDomainEvent {

    val timestamp: Long

    data class CreateNewTemplate(val id: Long, override val timestamp: Long) : EditTemplateDomainEvent

    data class RenameTemplate(val id: Long, val newTitle: String, override val timestamp: Long) :
        EditTemplateDomainEvent
}
