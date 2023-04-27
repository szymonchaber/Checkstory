package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import timber.log.Timber

sealed interface ViewTemplateCheckbox : java.io.Serializable {

    val id: TemplateCheckboxId
    val parentId: TemplateCheckboxId?
    val title: String
    val children: List<ViewTemplateCheckbox>

    val placeholderTitle: String?

    fun toDomainModel(parentId: TemplateCheckboxId? = null, position: Int): TemplateCheckbox

    fun minusChildCheckboxRecursive(checkbox: ViewTemplateCheckbox): ViewTemplateCheckbox {
        return withoutChild(checkbox.id) {}
    }

    fun withoutChild(
        childTaskId: TemplateCheckboxId,
        onItemFoundAndRemoved: (ViewTemplateCheckbox) -> Unit
    ): ViewTemplateCheckbox {
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
        parentTask: TemplateCheckboxId,
        childTask: ViewTemplateCheckbox
    ): ViewTemplateCheckbox {
        val updatedChildren = if (id == parentTask) {
            children.withCheckboxAtIndex(childTask.updateParentId(id), 0)
        } else {
            children.map { it.withMovedChildRecursive(parentTask, childTask) }
        }
        return abstractCopy(children = updatedChildren)
    }

    fun withMovedSiblingRecursive(
        siblingViewKey: TemplateCheckboxId,
        movedItem: ViewTemplateCheckbox
    ): ViewTemplateCheckbox {
        val siblingIndex = children.indexOfFirst { it.id == siblingViewKey }
        val isSiblingOnThisLevel = siblingIndex > -1
        val updatedChildren = if (isSiblingOnThisLevel) {
            children.withCheckboxAtIndex(movedItem.updateParentId(this.id), siblingIndex + 1)
        } else {
            children.map {
                it.withMovedSiblingRecursive(siblingViewKey, movedItem)
            }
        }
        return abstractCopy(children = updatedChildren)
    }

    fun withUpdatedTitleRecursive(checkbox: ViewTemplateCheckbox, newTitle: String): ViewTemplateCheckbox {
        return if (id == checkbox.id) {
            abstractCopy(title = newTitle)
        } else {
            abstractCopy(children = children.map { it.withUpdatedTitleRecursive(checkbox, newTitle) })
        }
    }

    fun plusChildCheckboxRecursive(
        parentId: TemplateCheckboxId,
        newCheckboxId: TemplateCheckboxId,
        placeholderTitle: String? = null
    ): ViewTemplateCheckbox {
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

    fun withIsLastChild(isLastChild: Boolean): ViewTemplateCheckbox {
        return abstractCopy()
    }

    fun updateParentId(parentId: TemplateCheckboxId?): ViewTemplateCheckbox {
        val updatedCheckbox = abstractCopy(parentId = parentId)
        return updatedCheckbox.abstractCopy(children = updatedCheckbox.children.updateParentIds(updatedCheckbox.id))
    }

    fun abstractCopy(
        id: TemplateCheckboxId = this.id,
        parentId: TemplateCheckboxId? = this.parentId,
        isParent: Boolean = false,
        title: String = this.title,
        children: List<ViewTemplateCheckbox> = this.children
    ): ViewTemplateCheckbox

    data class New(
        override val id: TemplateCheckboxId,
        override val parentId: TemplateCheckboxId?,
        override val title: String,
        override val children: List<ViewTemplateCheckbox>,
        override val placeholderTitle: String? = null
    ) : ViewTemplateCheckbox {

        override fun toDomainModel(parentId: TemplateCheckboxId?, position: Int): TemplateCheckbox {
            return TemplateCheckbox(
                id = id,
                parentId = parentId,
                title = title,
                children = children.mapIndexed { index, it ->
                    it.toDomainModel(id, index)
                },
                position.toLong()
            )
        }

        override fun abstractCopy(
            id: TemplateCheckboxId,
            parentId: TemplateCheckboxId?,
            isParent: Boolean,
            title: String,
            children: List<ViewTemplateCheckbox>,
        ): ViewTemplateCheckbox {
            return copy(
                id = id,
                parentId = parentId,
                title = title,
                children = children,
            )
        }
    }

    data class Existing(
        override val id: TemplateCheckboxId,
        override val parentId: TemplateCheckboxId?,
        override val title: String,
        override val children: List<ViewTemplateCheckbox>,
        override val placeholderTitle: String? = null
    ) : ViewTemplateCheckbox {

        override fun toDomainModel(parentId: TemplateCheckboxId?, position: Int): TemplateCheckbox {
            return TemplateCheckbox(
                id = id,
                parentId = parentId,
                title = title,
                children = children.mapIndexed { index, child ->
                    child.toDomainModel(id, index)
                },
                position.toLong()
            )
        }

        override fun abstractCopy(
            id: TemplateCheckboxId,
            parentId: TemplateCheckboxId?,
            isParent: Boolean,
            title: String,
            children: List<ViewTemplateCheckbox>,
        ): ViewTemplateCheckbox {
            return copy(
                id = id,
                parentId = parentId,
                title = title,
                children = children
            )
        }

        companion object {

            fun fromDomainModel(
                templateCheckbox: TemplateCheckbox
            ): Existing {
                return with(templateCheckbox) {
                    Existing(
                        id = this.id,
                        parentId = templateCheckbox.parentId,
                        title = title,
                        children = children.map {
                            fromDomainModel(it)
                        }.reindexed(),
                    ).also {
                        Timber.d("Building ${templateCheckbox.title} with key ${it.id}")
                    }
                }
            }
        }
    }
}

private fun List<ViewTemplateCheckbox>.reindexed(): List<ViewTemplateCheckbox> {
    return mapIndexed { index, item ->
        item.withIsLastChild(index == lastIndex)
    }
}

fun renderCheckbox(checkbox: ViewTemplateCheckbox, prefix: String = "     "): String {
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
