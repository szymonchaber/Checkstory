package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox

sealed interface EditTemplateCheckbox {

    val checkbox: TemplateCheckbox

    data class Existing(override val checkbox: TemplateCheckbox) : EditTemplateCheckbox

    data class New(override val checkbox: TemplateCheckbox) : EditTemplateCheckbox
}
