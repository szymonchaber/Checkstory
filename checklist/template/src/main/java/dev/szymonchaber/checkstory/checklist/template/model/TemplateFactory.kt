package dev.szymonchaber.checkstory.checklist.template.model

import android.content.res.Resources
import dev.szymonchaber.checkstory.checklist.template.R
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import kotlinx.datetime.Clock
import java.time.LocalDateTime
import java.util.*

fun generateOnboardingTemplate(resources: Resources): TemplateLoadingState.Success {
    val checklistTemplate = emptyChecklistTemplate()
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(checklistTemplate)
        .copy(
            commands = listOf(
                TemplateCommand.CreateNewTemplate(
                    checklistTemplate.id,
                    Clock.System.now()
                )
            )
        )

    return generateOnboardingCheckboxes()
        .foldInto(templateLoadingState)
        .copy(
            onboardingPlaceholders = OnboardingPlaceholders(
                title = resources.getString(R.string.onboarding_template_title),
                description = resources.getString(R.string.onboarding_template_description)
            ),
            isOnboardingTemplate = true
        )
}

class OnboardingCheckboxes {

    val placeholderToParentId = mutableListOf<Triple<TemplateCheckboxId, TemplateCheckboxId?, String>>()

    fun topLevelPlaceholder(placeholderTitle: String, block: ParentScope.() -> Unit = {}) {
        val parentId = TemplateCheckboxId(UUID.randomUUID())
        placeholderToParentId.add(Triple(parentId, null, placeholderTitle))
        ParentScope(parentId).block()
    }

    inner class ParentScope(private val outerParentId: TemplateCheckboxId) {

        fun nestedPlaceholder(placeholderTitle: String, block: ParentScope.() -> Unit = {}) {
            val innerParentId = TemplateCheckboxId(UUID.randomUUID())
            placeholderToParentId.add(Triple(innerParentId, outerParentId, placeholderTitle))
            ParentScope(innerParentId).block()
        }
    }
}

fun placeholderCheckboxes(block: OnboardingCheckboxes.() -> Unit): MutableList<Triple<TemplateCheckboxId, TemplateCheckboxId?, String>> {
    return OnboardingCheckboxes().apply(block).placeholderToParentId
}


fun generateOnboardingCheckboxes(): MutableList<Triple<TemplateCheckboxId, TemplateCheckboxId?, String>> {
    return placeholderCheckboxes {
        topLevelPlaceholder("Add as many tasks as you want")

        topLevelPlaceholder("Nest them as needed") {

            nestedPlaceholder("We think that it’s neat") {

                nestedPlaceholder("Nest them as needed") {

                    nestedPlaceholder("Up to four levels deep")

                }
            }
        }
        topLevelPlaceholder("You can add links like this:\ncheckstory.tech")

        topLevelPlaceholder("When you’re done,\nsave this template") {
            nestedPlaceholder("Then “use” it\non the next screen")
        }

        topLevelPlaceholder("Happy checklisting!")
    }
}

fun generateWriteOfferTemplate(): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyChecklistTemplate())
        .withNewTitle("Write an offer to a prospect")
        .withNewDescription("Find prospects here: drive.link/prospects")
    return generateWriteOfferCheckboxes().foldInto(templateLoadingState)
}

fun generateWriteOfferCheckboxes(): List<Triple<TemplateCheckboxId, TemplateCheckboxId?, String>> {
    return placeholderCheckboxes {
        topLevelPlaceholder("Fetch the prospect and start writing") {
            nestedPlaceholder("Write your own steps - Checkstory is a vessel for your knowledge") {
                nestedPlaceholder("You can nest your tasks") {
                    nestedPlaceholder("Up to four levels deep") {
                        nestedPlaceholder("as needed")
                    }
                }
            }
        }
        topLevelPlaceholder("Drop the offer for review here:\ndrive.link/offers")
    }
}

fun generateOnboardAnEmployeeTemplate(): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyChecklistTemplate())
        .withNewTitle("Onboard a new employee")
    return placeholderCheckboxes {
        repeat(18) {
            topLevelPlaceholder("Fluff to make the checklist look full of tasks")
        }
    }
        .foldInto(templateLoadingState)
}

fun generateDailyRoutineTemplate(): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyChecklistTemplate())
        .withNewDescription("Daily routine")

    return placeholderCheckboxes {
        repeat(5) {
            topLevelPlaceholder("Fluff to make the checklist look full of tasks")
        }
    }
        .foldInto(templateLoadingState)
}

fun List<Triple<TemplateCheckboxId, TemplateCheckboxId?, String>>.foldInto(success: TemplateLoadingState.Success): TemplateLoadingState.Success {
    return fold(success) { state, (id, parentId, placeholder) ->
        if (parentId != null) {
            state.plusChildCheckbox(parentId, id, placeholder)
        } else {
            state.plusNewCheckbox("", placeholderTitle = placeholder, id)
        }
    }.copy(mostRecentlyAddedItem = null)
}

fun emptyChecklistTemplate(): ChecklistTemplate {
    return ChecklistTemplate(
        ChecklistTemplateId.new(),
        "",
        "",
        listOf(),
        LocalDateTime.now(),
        listOf(),
        listOf()
    )
}
