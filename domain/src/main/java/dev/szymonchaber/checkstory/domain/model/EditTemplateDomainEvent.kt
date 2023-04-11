package dev.szymonchaber.checkstory.domain.model

sealed class EditTemplateDomainEvent {

    data class CreateNewTemplate(val id: Long) : EditTemplateDomainEvent()

    data class RenameTemplate(val id: Long, val newTitle: String) : EditTemplateDomainEvent()
}
