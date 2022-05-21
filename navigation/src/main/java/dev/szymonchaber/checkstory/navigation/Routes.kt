package dev.szymonchaber.checkstory.navigation

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

object Routes {

    fun newChecklistTemplateScreen(): String {
        return "edit_template_screen"
    }

    fun editChecklistTemplateScreen(templateId: ChecklistTemplateId): String {
        return "edit_template_screen?templateId=${templateId.id}"
    }
}