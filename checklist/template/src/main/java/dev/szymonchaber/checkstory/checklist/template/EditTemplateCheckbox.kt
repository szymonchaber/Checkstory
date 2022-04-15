package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import java.io.Serializable

sealed interface EditTemplateCheckbox : Serializable {

    val id: String

    val checkbox: TemplateCheckbox

    data class Existing(override val checkbox: TemplateCheckbox) : EditTemplateCheckbox {

        override val id: String
            get() = checkbox.id.id.toString()
    }

    data class New(override val checkbox: TemplateCheckbox) : EditTemplateCheckbox {

        override val id: String
            get() = "new_${checkbox.id.id}"
    }
}
