package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.checklist.template.viewKey
import dev.szymonchaber.checkstory.common.extensions.update
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

sealed interface ViewTemplateCheckbox : java.io.Serializable {

    val id: TemplateCheckboxId
    val parentViewKey: ViewTemplateCheckboxKey?
    val title: String
    val children: List<ViewTemplateCheckbox>
    val isParent: Boolean

    val isLastChild: Boolean

    fun withUpdatedTitle(title: String): ViewTemplateCheckbox {
        return abstractCopy(title = title)
    }

    fun toDomainModel(parentId: TemplateCheckboxId? = null, position: Int): TemplateCheckbox

    fun plusChildCheckbox(title: String): ViewTemplateCheckbox {
        return abstractCopy(
            children = children.plus(
                New(
                    TemplateCheckboxId(children.size.toLong()),
                    viewKey,
                    false,
                    title,
                    listOf(),
                    true
                )
            ).reindexed()
        )
    }

    fun minusChildCheckboxRecursive(checkbox: ViewTemplateCheckbox): ViewTemplateCheckbox {
        return abstractCopy(
            children = children
                .filterNot { it.viewKey == checkbox.viewKey }
                .map { it.minusChildCheckboxRecursive(checkbox) }
        )
    }

    fun editChildCheckboxTitle(child: ViewTemplateCheckbox, title: String): ViewTemplateCheckbox {
        return abstractCopy(
            children = children.update(child.viewKey) {
                it.withUpdatedTitle(title)
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
        override val isLastChild: Boolean
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
        override val isLastChild: Boolean
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

            fun fromDomainModel(templateCheckbox: TemplateCheckbox): Existing {
                return with(templateCheckbox) {
                    Existing(
                        id = id,
                        parentViewKey = parentId?.let {
                            ViewTemplateCheckboxKey(viewId = it.id, isNew = false, isParent = true, parentKey = null)
                        },
                        parentId == null,
                        title = title,
                        children = children.map { fromDomainModel(it) }.reindexed(),
                        false
                    )
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

fun List<ViewTemplateCheckbox>.update(
    viewTemplateCheckboxKey: ViewTemplateCheckboxKey,
    updater: (ViewTemplateCheckbox) -> ViewTemplateCheckbox
): List<ViewTemplateCheckbox> {
    return update(
        viewTemplateCheckboxKey,
        {
            it.viewKey
        },
        updater
    )
}

fun renderCheckbox(checkbox: ViewTemplateCheckbox): String {
    val parentIndicator = if (checkbox.isParent) "Parent " else "Child "
    val children = checkbox.children.takeUnless { it.isEmpty() }?.joinToString("\n") { child ->
        val childIndicator = if (child.isParent) "Parent " else "Child "
        "     $childIndicator ${child.title}"
    }
    return parentIndicator + " " + checkbox.title + children?.let { "\n" + it }.orEmpty()
}
