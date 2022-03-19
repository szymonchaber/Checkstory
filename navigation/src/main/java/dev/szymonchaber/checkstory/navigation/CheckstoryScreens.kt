package dev.szymonchaber.checkstory.navigation

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed class CheckstoryScreens(val route: String) {

    object HomeScreen : CheckstoryScreens("home_screen")

    object DetailsScreen : CheckstoryScreens("details_screen?sourceChecklistTemplateId={sourceChecklistTemplateId}") {

        const val sourceChecklistTemplateIdArg = "sourceChecklistTemplateId"

        fun destination(checklistTemplateId: ChecklistTemplateId): String {
            return "details_screen?sourceChecklistTemplateId=${checklistTemplateId.id}"
        }
    }
}
