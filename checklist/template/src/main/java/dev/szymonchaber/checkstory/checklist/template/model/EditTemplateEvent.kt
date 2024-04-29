package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface EditTemplateEvent {

    data object CreateTemplate : EditTemplateEvent

    data object GenerateOnboardingTemplate : EditTemplateEvent

    data class EditTemplate(val templateId: TemplateId) : EditTemplateEvent

    data class TitleChanged(val newTitle: String) : EditTemplateEvent

    data class DescriptionChanged(val newDescription: String) : EditTemplateEvent

    data class TaskRemoved(val task: ViewTemplateTask) : EditTemplateEvent

    data class TaskTitleChanged(val task: ViewTemplateTask, val newTitle: String) : EditTemplateEvent

    data class ChildTaskAdded(val parentId: TemplateTaskId) : EditTemplateEvent

    data object AddReminderClicked : EditTemplateEvent

    data class ReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class ReminderSaved(val reminder: Reminder) : EditTemplateEvent

    data class DeleteReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class SiblingMovedBelow(
        val target: TemplateTaskId,
        val newSibling: TemplateTaskId
    ) : EditTemplateEvent

    data class ChildMovedBelow(
        val target: TemplateTaskId,
        val newChild: TemplateTaskId
    ) : EditTemplateEvent

    data class NewChildDraggedBelow(val target: TemplateTaskId) : EditTemplateEvent

    data object NewTaskDraggedToTop : EditTemplateEvent

    data object NewTaskDraggedToBottom : EditTemplateEvent

    data class TaskMovedToTop(val taskId: TemplateTaskId) : EditTemplateEvent

    data class TaskMovedToBottom(val taskId: TemplateTaskId) : EditTemplateEvent

    data object AddTaskClicked : EditTemplateEvent

    data object SaveTemplateClicked : EditTemplateEvent

    data object DeleteTemplateClicked : EditTemplateEvent

    data object ConfirmDeleteTemplateClicked : EditTemplateEvent

    data object BackClicked : EditTemplateEvent

    data object ConfirmExitClicked : EditTemplateEvent

    data object TemplateHistoryClicked : EditTemplateEvent
}
