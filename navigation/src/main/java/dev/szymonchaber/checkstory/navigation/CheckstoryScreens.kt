package dev.szymonchaber.checkstory.navigation

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed class CheckstoryScreens(val route: String) {

    object HomeScreen : CheckstoryScreens("home_screen")

    object DetailsScreen :
        CheckstoryScreens("details_screen?checklistId={checklistId}&sourceChecklistTemplateId={sourceChecklistTemplateId}") {

        const val sourceChecklistTemplateIdArg = "sourceChecklistTemplateId"
        const val checklistIdArg = "checklistId"

        fun createChecklist(checklistTemplateId: ChecklistTemplateId): String {
            return "details_screen?sourceChecklistTemplateId=${checklistTemplateId.id}"
        }

        fun goToChecklist(checklistId: ChecklistId): String {
            return "details_screen?checklistId=${checklistId.id}"
        }
    }

    object EditTemplateScreen : CheckstoryScreens("template_screen?templateId={templateId}") {

        const val templateIdArg = "templateId"

        fun createTemplate(): String {
            return route
        }

        fun editTemplate(templateId: ChecklistTemplateId): String {
            return "template_screen?templateId=${templateId.id}"
        }
    }
}
