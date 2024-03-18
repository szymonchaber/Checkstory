package dev.szymonchaber.checkstory.checklist.template.model

import android.app.Application
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import kotlinx.datetime.Clock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class OnboardingTemplateFactory @Inject constructor(private val application: Application) {

    fun generateOnboardingTemplate(): TemplateLoadingState.Success {
        val template = emptyTemplate()
        val templateLoadingState = TemplateLoadingState.Success.fromTemplate(template)
            .copy(
                commands = listOf(
                    TemplateCommand.CreateNewTemplate(
                        template.id,
                        Clock.System.now()
                    )
                )
            )

        return generateOnboardingTasks()
            .foldInto(templateLoadingState)
            .copy(
                onboardingPlaceholders = OnboardingPlaceholders(
                    title = application.resources.getString(R.string.onboarding_template_title),
                    description = application.resources.getString(R.string.onboarding_template_description)
                ),
            )
    }
}

class OnboardingTasks {

    val placeholderToParentId = mutableListOf<Triple<TemplateTaskId, TemplateTaskId?, String>>()

    fun topLevelPlaceholder(placeholderTitle: String, block: ParentScope.() -> Unit = {}) {
        val parentId = TemplateTaskId(UUID.randomUUID())
        placeholderToParentId.add(Triple(parentId, null, placeholderTitle))
        ParentScope(parentId).block()
    }

    inner class ParentScope(private val outerParentId: TemplateTaskId) {

        fun nestedPlaceholder(placeholderTitle: String, block: ParentScope.() -> Unit = {}) {
            val innerParentId = TemplateTaskId(UUID.randomUUID())
            placeholderToParentId.add(Triple(innerParentId, outerParentId, placeholderTitle))
            ParentScope(innerParentId).block()
        }
    }
}

fun placeholderTasks(block: OnboardingTasks.() -> Unit): MutableList<Triple<TemplateTaskId, TemplateTaskId?, String>> {
    return OnboardingTasks().apply(block).placeholderToParentId
}

fun generateOnboardingTasks(): MutableList<Triple<TemplateTaskId, TemplateTaskId?, String>> {
    return placeholderTasks {
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
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyTemplate())
        .withNewTitle("Write an offer to a prospect")
        .withNewDescription("Find prospects here: drive.link/prospects")
    return generateWriteOfferTasks().foldInto(templateLoadingState)
}

fun generateWriteOfferTasks(): List<Triple<TemplateTaskId, TemplateTaskId?, String>> {
    return placeholderTasks {
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
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyTemplate())
        .withNewTitle("Onboard a new employee")
    return placeholderTasks {
        repeat(18) {
            topLevelPlaceholder("Fluff to make the checklist look full of tasks")
        }
    }
        .foldInto(templateLoadingState)
}

fun generateDailyRoutineTemplate(): TemplateLoadingState.Success {
    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(emptyTemplate())
        .withNewDescription("Daily routine")

    return placeholderTasks {
        repeat(5) {
            topLevelPlaceholder("Fluff to make the checklist look full of tasks")
        }
    }
        .foldInto(templateLoadingState)
}

fun List<Triple<TemplateTaskId, TemplateTaskId?, String>>.foldInto(success: TemplateLoadingState.Success): TemplateLoadingState.Success {
    return fold(success) { state, (id, parentId, placeholder) ->
        if (parentId != null) {
            state.plusChildTask(parentId, id, placeholder)
        } else {
            state.plusNewTask("", placeholderTitle = placeholder, id)
        }
    }.copy(mostRecentlyAddedItem = null)
}

fun emptyTemplate(): Template {
    return Template(
        TemplateId.new(),
        "",
        "",
        listOf(),
        LocalDateTime.now(),
        listOf(),
        listOf()
    )
}
