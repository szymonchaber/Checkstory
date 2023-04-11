package dev.szymonchaber.checkstory.domain.model

sealed class EditTemplateDomainEvent {

    class CreateNewTemplate(val id: Long) : EditTemplateDomainEvent()
}
