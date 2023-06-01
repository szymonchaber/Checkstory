package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId

data class ViewTemplateTask(
    val id: TemplateTaskId,
    val parentId: TemplateTaskId?,
    val title: String,
    val children: List<ViewTemplateTask>,
    val placeholderTitle: String? = null
) : java.io.Serializable {

    fun toDomainModel(
        position: Int,
        templateId: TemplateId,
        parentId: TemplateTaskId? = null
    ): TemplateTask {
        return TemplateTask(
            id = id,
            parentId = parentId,
            title = title,
            children = children.mapIndexed { index, child ->
                child.toDomainModel(index, templateId, id)
            },
            sortPosition = position.toLong(),
            templateId = templateId
        )
    }

    fun minusChildTaskRecursive(checkbox: ViewTemplateTask): ViewTemplateTask {
        return withoutChild(checkbox.id) {}
    }

    fun withoutChild(
        childTaskId: TemplateTaskId,
        onItemFoundAndRemoved: (ViewTemplateTask) -> Unit
    ): ViewTemplateTask {
        val updatedChildren = children
            .firstOrNull {
                it.id == childTaskId
            }
            ?.let {
                onItemFoundAndRemoved(it)
                children.minus(it)
            }
            ?: children.map {
                it.withoutChild(childTaskId, onItemFoundAndRemoved)
            }
        return copy(children = updatedChildren)
    }

    fun withMovedChildRecursive(
        parentTask: TemplateTaskId,
        childTask: ViewTemplateTask
    ): ViewTemplateTask {
        val updatedChildren = if (id == parentTask) {
            children.withTaskAtIndex(childTask.updateParentId(id), 0)
        } else {
            children.map { it.withMovedChildRecursive(parentTask, childTask) }
        }
        return copy(children = updatedChildren)
    }

    fun withMovedSiblingRecursive(
        siblingId: TemplateTaskId,
        movedItem: ViewTemplateTask
    ): ViewTemplateTask {
        val siblingIndex = children.indexOfFirst { it.id == siblingId }
        val isSiblingOnThisLevel = siblingIndex > -1
        val updatedChildren = if (isSiblingOnThisLevel) {
            children.withTaskAtIndex(movedItem.updateParentId(this.id), siblingIndex + 1)
        } else {
            children.map {
                it.withMovedSiblingRecursive(siblingId, movedItem)
            }
        }
        return copy(children = updatedChildren)
    }

    fun withUpdatedTitleRecursive(checkbox: ViewTemplateTask, newTitle: String): ViewTemplateTask {
        return if (id == checkbox.id) {
            copy(title = newTitle)
        } else {
            copy(children = children.map { it.withUpdatedTitleRecursive(checkbox, newTitle) })
        }
    }

    fun plusChildCheckboxRecursive(
        parentId: TemplateTaskId,
        newCheckboxId: TemplateTaskId,
        placeholderTitle: String? = null
    ): ViewTemplateTask {
        return copy(
            children = if (id == parentId) {
                children.plus(
                    ViewTemplateTask(
                        id = newCheckboxId,
                        parentId = id,
                        title = "",
                        children = listOf(),
                        placeholderTitle = placeholderTitle
                    )
                )
            } else {
                children.map {
                    it.plusChildCheckboxRecursive(
                        parentId,
                        newCheckboxId,
                        placeholderTitle
                    )
                }
            }
        )
    }

    fun updateParentId(parentId: TemplateTaskId?): ViewTemplateTask {
        val updatedCheckbox = copy(parentId = parentId)
        return updatedCheckbox.copy(children = updatedCheckbox.children.updateParentIds(updatedCheckbox.id))
    }

    companion object {

        fun fromDomainModel(templateTask: TemplateTask): ViewTemplateTask {
            return with(templateTask) {
                ViewTemplateTask(
                    id = id,
                    parentId = templateTask.parentId,
                    title = title,
                    children = children.map(::fromDomainModel),
                )
            }
        }
    }
}

fun renderCheckbox(checkbox: ViewTemplateTask, prefix: String = "     "): String {
    val children = checkbox.children.joinToString("\n") { child ->
        "$prefix${child.title.ifEmpty { "Empty" }} \n${
            child.children.joinToString("") {
                "     ${
                    renderCheckbox(
                        it,
                        "$prefix     "
                    )
                }"
            }
        }"
    }
    return "${checkbox.title.ifEmpty { "Empty" }} ${children.let { "\n$it" }}"
}

fun printTemplateTask(templateTask: TemplateTask, prefix: String = "") {
    println("$prefix|----${templateTask.title}")
    templateTask.children.forEach { child ->
        printTemplateTask(child, "$prefix    ")
    }
}
