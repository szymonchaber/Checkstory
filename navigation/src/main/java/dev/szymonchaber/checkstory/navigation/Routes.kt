package dev.szymonchaber.checkstory.navigation

import com.ramcosta.composedestinations.spec.Direction
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

object Routes {

    fun onboardingTemplateScreen(): Direction {
        return "edit_template_screen?generateOnboarding=true".toDirection()
    }

    fun newTemplateScreen(): Direction {
        return "edit_template_screen".toDirection()
    }

    fun editTemplateScreen(templateId: TemplateId): Direction {
        return "edit_template_screen?templateId=${templateId.id}".toDirection()
    }

    fun checklistHistoryScreen(templateId: TemplateId): Direction {
        return "checklist_history?templateId=${templateId.id}".toDirection()
    }

    fun newChecklistScreen(templateId: TemplateId): Direction {
        return "fill_checklist_screen?createChecklistFrom=${templateId.id}".toDirection()
    }

    fun editChecklistScreen(checklistId: ChecklistId): Direction {
        return "fill_checklist_screen?checklistId=${checklistId.id}".toDirection()
    }

    fun paymentScreen(): Direction {
        return "payment_screen".toDirection()
    }

    fun aboutScreen(): Direction {
        return "about_screen".toDirection()
    }

    fun homeScreen() = "home_screen".toDirection()

    fun onboardingScreen() = "onboarding_screen".toDirection()

    fun accountScreen(
        triggerPartialRegistration: Boolean = false,
        triggerPurchaseRestoration: Boolean = false
    ): Direction {
        return ("account_screen" +
                "?triggerPartialRegistration=$triggerPartialRegistration" +
                "&triggerPurchaseRestoration=$triggerPurchaseRestoration").toDirection()
    }
}

internal fun String.toDirection(): Direction {
    return Direction(this)
}
