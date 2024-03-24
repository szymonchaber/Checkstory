package dev.szymonchaber.checkstory.navigation

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

object Routes {

    fun onboardingTemplateScreen(): String {
        return "edit_template_screen?generateOnboarding=true"
    }

    fun newTemplateScreen(): String {
        return "edit_template_screen"
    }

    fun editTemplateScreen(templateId: TemplateId): String {
        return "edit_template_screen?templateId=${templateId.id}"
    }

    fun checklistHistoryScreen(templateId: TemplateId): String {
        return "checklist_history?templateId=${templateId.id}"
    }

    fun newChecklistScreen(templateId: TemplateId): String {
        return "fill_checklist_screen?createChecklistFrom=${templateId.id}"
    }

    fun editChecklistScreen(checklistId: ChecklistId): String {
        return "fill_checklist_screen?checklistId=${checklistId.id}"
    }

    fun paymentScreen(): String {
        return "payment_screen"
    }

    fun aboutScreen(): String {
        return "about_screen"
    }

    fun homeScreen(): String = "home_screen"

    fun onboardingScreen() = "onboarding_screen"

    fun accountScreen(
        triggerPartialRegistration: Boolean = false,
        triggerPurchaseRestoration: Boolean = false
    ): String {
        return "account_screen" +
                "?triggerPartialRegistration=$triggerPartialRegistration" +
                "&triggerPurchaseRestoration=$triggerPurchaseRestoration"
    }
}
