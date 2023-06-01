package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId

sealed interface ViewTemplateTask : java.io.Serializable {

    val id: TemplateTaskId
    val parentId: TemplateTaskId?
    val title: String
    val children: List<ViewTemplateTask>

    val placeholderTitle: String?

    fun toDomainModel(
        parentId: TemplateTaskId? = null,
        position: Int,
        templateId: TemplateId
    ): TemplateTask

    fun minusChildCheckboxRecursive(checkbox: ViewTemplateTask): ViewTemplateTask {
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
        return abstractCopy(children = updatedChildren)
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
        return abstractCopy(children = updatedChildren)
    }

    fun withMovedSiblingRecursive(
        siblingViewKey: TemplateTaskId,
        movedItem: ViewTemplateTask
    ): ViewTemplateTask {
        val siblingIndex = children.indexOfFirst { it.id == siblingViewKey }
        val isSiblingOnThisLevel = siblingIndex > -1
        val updatedChildren = if (isSiblingOnThisLevel) {
            children.withTaskAtIndex(movedItem.updateParentId(this.id), siblingIndex + 1)
        } else {
            children.map {
                it.withMovedSiblingRecursive(siblingViewKey, movedItem)
            }
        }
        return abstractCopy(children = updatedChildren)
    }

    fun withUpdatedTitleRecursive(checkbox: ViewTemplateTask, newTitle: String): ViewTemplateTask {
        return if (id == checkbox.id) {
            abstractCopy(title = newTitle)
        } else {
            abstractCopy(children = children.map { it.withUpdatedTitleRecursive(checkbox, newTitle) })
        }
    }

    fun plusChildCheckboxRecursive(
        parentId: TemplateTaskId,
        newCheckboxId: TemplateTaskId,
        placeholderTitle: String? = null
    ): ViewTemplateTask {
        return abstractCopy(
            children = if (id == parentId) {
                children.plus(
                    New(
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

    fun withIsLastChild(isLastChild: Boolean): ViewTemplateTask {
        return abstractCopy()
    }

    fun updateParentId(parentId: TemplateTaskId?): ViewTemplateTask {
        val updatedCheckbox = abstractCopy(parentId = parentId)
        return updatedCheckbox.abstractCopy(children = updatedCheckbox.children.updateParentIds(updatedCheckbox.id))
    }

    fun abstractCopy(
        id: TemplateTaskId = this.id,
        parentId: TemplateTaskId? = this.parentId,
        isParent: Boolean = false,
        title: String = this.title,
        children: List<ViewTemplateTask> = this.children
    ): ViewTemplateTask

    data class New(
        override val id: TemplateTaskId,
        override val parentId: TemplateTaskId?,
        override val title: String,
        override val children: List<ViewTemplateTask>,
        override val placeholderTitle: String? = null
    ) : ViewTemplateTask {

        override fun toDomainModel(
            parentId: TemplateTaskId?,
            position: Int,
            templateId: TemplateId
        ): TemplateTask {
            return TemplateTask(
                id = id,
                parentId = parentId,
                title = title,
                children = children.mapIndexed { index, it ->
                    it.toDomainModel(id, index, templateId)
                },
                position.toLong(),
                templateId
            )
        }

        override fun abstractCopy(
            id: TemplateTaskId,
            parentId: TemplateTaskId?,
            isParent: Boolean,
            title: String,
            children: List<ViewTemplateTask>,
        ): ViewTemplateTask {
            return copy(
                id = id,
                parentId = parentId,
                title = title,
                children = children,
            )
        }
    }

    data class Existing(
        override val id: TemplateTaskId,
        override val parentId: TemplateTaskId?,
        override val title: String,
        override val children: List<ViewTemplateTask>,
        override val placeholderTitle: String? = null
    ) : ViewTemplateTask {

        override fun toDomainModel(
            parentId: TemplateTaskId?,
            position: Int,
            templateId: TemplateId
        ): TemplateTask {
            return TemplateTask(
                id = id,
                parentId = parentId,
                title = title,
                children = children.mapIndexed { index, child ->
                    child.toDomainModel(id, index, templateId)
                },
                position.toLong(),
                templateId
            )
        }

        override fun abstractCopy(
            id: TemplateTaskId,
            parentId: TemplateTaskId?,
            isParent: Boolean,
            title: String,
            children: List<ViewTemplateTask>,
        ): ViewTemplateTask {
            return copy(
                id = id,
                parentId = parentId,
                title = title,
                children = children
            )
        }

        companion object {

            fun fromDomainModel(
                templateTask: TemplateTask
            ): Existing {
                return with(templateTask) {
                    Existing(
                        id = this.id,
                        parentId = templateTask.parentId,
                        title = title,
                        children = children.map {
                            fromDomainModel(it)
                        }.reindexed(),
                    )
                }
            }
        }
    }
}

private fun List<ViewTemplateTask>.reindexed(): List<ViewTemplateTask> {
    return mapIndexed { index, item ->
        item.withIsLastChild(index == lastIndex)
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
