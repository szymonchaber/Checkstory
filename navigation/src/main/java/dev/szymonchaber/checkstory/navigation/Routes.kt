package dev.szymonchaber.checkstory.navigation

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

object Routes {

    fun onboardingChecklistTemplateScreen(): String {
        return "edit_template_screen?generateOnboarding=true"
    }

    fun newChecklistTemplateScreen(): String {
        return "edit_template_screen"
    }

    fun editChecklistTemplateScreen(templateId: ChecklistTemplateId): String {
        return "edit_template_screen?templateId=${templateId.id}"
    }

    fun checklistHistoryScreen(templateId: ChecklistTemplateId): String {
        return "checklist_history?templateId=${templateId.id}"
    }

    fun newChecklistScreen(templateId: ChecklistTemplateId): String {
        return "fill_checklist_screen?checklistId={checklistId}?createChecklistFrom=${templateId.id}"
    }

    fun editChecklistScreen(checklistId: ChecklistId): String {
        return "fill_checklist_screen?checklistId=${checklistId.id}?createChecklistFrom={createChecklistFrom}"
    }

    fun paymentScreen(): String {
        return "payment_screen"
    }

    fun aboutScreen(): String {
        return "about_screen"
    }

    fun homeScreen(): String = "home_screen"

    fun onboardingScreen() = "onboarding_screen"
}
