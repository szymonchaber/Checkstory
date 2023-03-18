package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.checklist.template.viewKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import timber.log.Timber

sealed interface ViewTemplateCheckbox : java.io.Serializable {

    val id: TemplateCheckboxId
    val parentViewKey: ViewTemplateCheckboxKey?
    val title: String
    val children: List<ViewTemplateCheckbox>
    val isParent: Boolean

    val isLastChild: Boolean

    val placeholderTitle: String?

    fun toDomainModel(parentId: TemplateCheckboxId? = null, position: Int): TemplateCheckbox

    fun minusChildCheckboxRecursive(checkbox: ViewTemplateCheckbox): ViewTemplateCheckbox {
        return withoutChild(checkbox.viewKey) {}
    }

    fun withoutChild(
        childTaskViewKey: ViewTemplateCheckboxKey,
        onItemFoundAndRemoved: (ViewTemplateCheckbox) -> Unit
    ): ViewTemplateCheckbox {
        val updatedChildren = children
            .firstOrNull {
                it.viewKey == childTaskViewKey
            }
            ?.let {
                onItemFoundAndRemoved(it)
                children.minus(it)
            }
            ?: children.map {
                it.withoutChild(childTaskViewKey, onItemFoundAndRemoved)
            }
        return abstractCopy(children = updatedChildren)
    }

    fun withMovedChildRecursive(
        parentTask: ViewTemplateCheckboxKey,
        childTask: ViewTemplateCheckbox
    ): ViewTemplateCheckbox {
        val updatedChildren = if (viewKey == parentTask) {
            children.withCheckboxAtIndex(childTask, 0)
        } else {
            children.map { it.withMovedChildRecursive(parentTask, childTask) }
        }
        return abstractCopy(children = updatedChildren)
    }

    fun withMovedSiblingRecursive(
        siblingViewKey: ViewTemplateCheckboxKey,
        movedItem: ViewTemplateCheckbox
    ): ViewTemplateCheckbox {
        val siblingIndex = children.indexOfFirst { it.viewKey == siblingViewKey }
        val isSiblingOnThisLevel = siblingIndex > -1
        val updatedChildren = if (isSiblingOnThisLevel) {
            children.withCheckboxAtIndex(movedItem, siblingIndex + 1)
        } else {
            children.map {
                it.withMovedSiblingRecursive(siblingViewKey, movedItem)
            }
        }
        return abstractCopy(children = updatedChildren)
    }

    fun withUpdatedTitleRecursive(checkbox: ViewTemplateCheckbox, newTitle: String): ViewTemplateCheckbox {
        return if (viewKey == checkbox.viewKey) {
            abstractCopy(title = newTitle)
        } else {
            abstractCopy(children = children.map { it.withUpdatedTitleRecursive(checkbox, newTitle) })
        }
    }

    fun plusChildCheckboxRecursive(
        parentId: ViewTemplateCheckboxKey,
        onItemAdded: (ViewTemplateCheckboxKey) -> Unit
    ): ViewTemplateCheckbox {
        return abstractCopy(
            children = if (viewKey == parentId) {
                children.plus(
                    New(
                        TemplateCheckboxId(children.size.toLong()),
                        viewKey,
                        false,
                        "",
                        listOf(),
                        true
                    ).also { newItem ->
                        onItemAdded(newItem.viewKey)
                    }
                )
            } else {
                children.map {
                    it.plusChildCheckboxRecursive(parentId, onItemAdded)
                }
            }
        )
    }

    fun plusNestedChildCheckboxRecursive(
        parentId: ViewTemplateCheckboxKey,
        placeholderTitle: String = "",
        checkboxes: List<CheckboxToChildren>
    ): ViewTemplateCheckbox {
        return abstractCopy(
            children = if (viewKey == parentId) {
                val newElement = New(
                    id = TemplateCheckboxId(children.size.toLong()),
                    parentViewKey = viewKey,
                    isParent = false,
                    title = "",
                    children = listOf(),
                    isLastChild = true,
                    placeholderTitle = placeholderTitle
                )
                val newUpdatedElement =
                    checkboxes.fold(newElement) { acc: ViewTemplateCheckbox, checkboxToChildren ->
                        acc.plusNestedChildCheckboxRecursive(
                            newElement.viewKey,
                            checkboxToChildren.placeholderTitle,
                            checkboxToChildren.children
                        )
                    }
                children.plus(newUpdatedElement)
            } else {
                children.map { it.plusNestedChildCheckboxRecursive(parentId, placeholderTitle, checkboxes) }
            }
        )
    }

    fun replaceChildren(children: List<ViewTemplateCheckbox>): ViewTemplateCheckbox {
        return abstractCopy(children = children.reindexed())
    }

    fun withIsLastChild(isLastChild: Boolean): ViewTemplateCheckbox {
        return abstractCopy(isLastChild = isLastChild)
    }

    fun abstractCopy(
        id: TemplateCheckboxId = this.id,
        parentViewKey: ViewTemplateCheckboxKey? = this.parentViewKey,
        isParent: Boolean = this.isParent,
        title: String = this.title,
        children: List<ViewTemplateCheckbox> = this.children,
        isLastChild: Boolean = this.isLastChild
    ): ViewTemplateCheckbox

    data class New(
        override val id: TemplateCheckboxId,
        override val parentViewKey: ViewTemplateCheckboxKey?,
        override val isParent: Boolean,
        override val title: String,
        override val children: List<ViewTemplateCheckbox>,
        override val isLastChild: Boolean,
        override val placeholderTitle: String? = null
    ) : ViewTemplateCheckbox {

        override fun toDomainModel(parentId: TemplateCheckboxId?, position: Int): TemplateCheckbox {
            return TemplateCheckbox(
                id = TemplateCheckboxId(0),
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
            parentViewKey: ViewTemplateCheckboxKey?,
            isParent: Boolean,
            title: String,
            children: List<ViewTemplateCheckbox>,
            isLastChild: Boolean,
        ): ViewTemplateCheckbox {
            return copy(
                id = id,
                parentViewKey = parentViewKey,
                isParent = isParent,
                title = title,
                children = children,
                isLastChild = isLastChild
            )
        }
    }

    data class Existing(
        override val id: TemplateCheckboxId,
        override val parentViewKey: ViewTemplateCheckboxKey?,
        override val isParent: Boolean,
        override val title: String,
        override val children: List<ViewTemplateCheckbox>,
        override val isLastChild: Boolean,
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
            parentViewKey: ViewTemplateCheckboxKey?,
            isParent: Boolean,
            title: String,
            children: List<ViewTemplateCheckbox>,
            isLastChild: Boolean,
        ): ViewTemplateCheckbox {
            return copy(
                id = id,
                parentViewKey = parentViewKey,
                isParent = isParent,
                title = title,
                children = children,
                isLastChild = isLastChild
            )
        }

        companion object {

            fun fromDomainModel(
                templateCheckbox: TemplateCheckbox,
                ancestorViewKey: ViewTemplateCheckboxKey? = templateCheckbox.parentId?.let {
                    ViewTemplateCheckboxKey(viewId = it.id, parentKey = null, isNew = false)
                }
            ): Existing {
                return with(templateCheckbox) {
                    Existing(
                        id = this.id,
                        parentViewKey = ancestorViewKey,
                        parentId == null,
                        title = title,
                        children = children.map {
                            fromDomainModel(it, it.parentId?.let {
                                ViewTemplateCheckboxKey(viewId = it.id, parentKey = ancestorViewKey, isNew = false)
                            })
                        }.reindexed(),
                        false
                    ).also {
                        Timber.d("Building ${templateCheckbox.title} with key ${it.viewKey}")
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
