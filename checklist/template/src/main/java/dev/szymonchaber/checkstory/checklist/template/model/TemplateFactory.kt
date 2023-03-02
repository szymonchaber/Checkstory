package dev.szymonchaber.checkstory.checklist.template.model

import android.content.res.Resources
import dev.szymonchaber.checkstory.checklist.template.R
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.time.LocalDateTime

fun generateOnboardingTemplate(resources: Resources): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyChecklistTemplate())
    return generateOnboardingCheckboxes()
        .fold(templateLoadingState) { state, checkboxToChildren ->
            state.plusNestedCheckbox(checkboxToChildren.title, checkboxToChildren.children)
        }
        .copy(
            onboardingPlaceholders = OnboardingPlaceholders(
                title = resources.getString(R.string.onboarding_template_title),
                description = resources.getString(R.string.onboarding_template_description)
            )
        )
}

fun generateOnboardingCheckboxes(): List<CheckboxToChildren> {
    return listOf(
        CheckboxToChildren("Add as many tasks as you want"),
        CheckboxToChildren(
            "Nest them as needed", listOf(
                CheckboxToChildren(
                    "We think that it’s neat", listOf(
                        CheckboxToChildren(
                            "Nest them as needed", listOf(
                                CheckboxToChildren(
                                    "Up to four levels deep"
                                )
                            )
                        )
                    )
                )
            )
        ),
        CheckboxToChildren("You can add links like this:\ncheckstory.tech"),
        CheckboxToChildren(
            "When you’re done,\nsave this template", listOf(
                CheckboxToChildren("Then “use” it\non the next screen")
            )
        ),
        CheckboxToChildren("Happy checklisting!")
    )
}

fun generateWriteOfferTemplate(): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyChecklistTemplate())
        .updateTemplate {
            copy(
                title = "Write an offer to a prospect",
                description = "Find prospects here: drive.link/prospects"
            )
        }
    val withChildren = generateWriteOfferCheckboxes()
        .fold(templateLoadingState) { state, checkboxToChildren ->
            state.plusNestedCheckbox(checkboxToChildren.title, checkboxToChildren.children)
        }
    return withChildren
}

fun generateWriteOfferCheckboxes(): List<CheckboxToChildren> {
    return listOf(
        CheckboxToChildren("Fetch the prospect and start writing"),
        CheckboxToChildren(
            "Write your own steps - Checkstory is a vessel for your knowledge", listOf(
                CheckboxToChildren(
                    "You can nest your tasks", listOf(
                        CheckboxToChildren(
                            "Up to four levels deep", listOf(
                                CheckboxToChildren(
                                    "as needed"
                                )
                            )
                        )
                    )
                )
            )
        ),
        CheckboxToChildren("Drop the offer for review here:\ndrive.link/offers"),
    )
}

fun generateOnboardAnEmployeeTemplate(): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyChecklistTemplate())
        .updateTemplate {
            copy(
                title = "Onboard a new employee",
                description = ""
            )
        }
    return List(18) {
        CheckboxToChildren("$it")
    }
        .fold(templateLoadingState) { state, checkboxToChildren ->
            state.plusNestedCheckbox(checkboxToChildren.title, checkboxToChildren.children)
        }
}

fun generateDailyRoutineTemplate(): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyChecklistTemplate())
        .updateTemplate {
            copy(
                title = "Daily routine",
                description = ""
            )
        }
    return List(5) {
        CheckboxToChildren("$it")
    }
        .fold(templateLoadingState) { state, checkboxToChildren ->
            state.plusNestedCheckbox(checkboxToChildren.title, checkboxToChildren.children)
        }
}


fun emptyChecklistTemplate(): ChecklistTemplate {
    return ChecklistTemplate(
        ChecklistTemplateId(0),
        "",
        "",
        listOf(),
        LocalDateTime.now(),
        listOf(),
        listOf()
    )
}
