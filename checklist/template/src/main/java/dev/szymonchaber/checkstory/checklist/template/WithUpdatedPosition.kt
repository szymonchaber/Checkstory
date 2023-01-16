package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import org.burnoutcrew.reorderable.ItemPosition
import timber.log.Timber

fun withUpdatedPosition(
    checkboxes: List<ViewTemplateCheckbox>,
    from: ItemPosition,
    to: ItemPosition
): List<ViewTemplateCheckbox> {
    return checkboxes.toMutableList()
        .apply {
            Timber.d(
                "Moving:\n" +
                        "from: ${from.key}\n" +
                        "to: ${to.key}"
            )
            Timber.d("Items before moving:\n${renderList(this)}")
            val fromCheckbox = from.checkbox!!
            val toCheckbox = to.checkbox!!
            if (areParentsMoving(fromCheckbox, toCheckbox)) {
                moveParent(fromCheckbox, toCheckbox)
            }
            if (isChildMoving(fromCheckbox)) {
                moveChild(fromCheckbox, toCheckbox)
            }
            Timber.d("Items after moving:\n${renderList(this)}")
        }
}

private fun isChildMoving(fromCheckbox: ViewTemplateCheckbox) = fromCheckbox.isChild

private fun areParentsMoving(fromCheckbox: ViewTemplateCheckbox, toCheckbox: ViewTemplateCheckbox): Boolean {
    return fromCheckbox.isParent && toCheckbox.isParent
}

private fun MutableList<ViewTemplateCheckbox>.moveParent(
    checkbox: ViewTemplateCheckbox,
    toPositionOf: ViewTemplateCheckbox
) {
    val toIndex = indexOfFirst { it == toPositionOf }
    add(toIndex, removeAt(indexOfFirst { it == checkbox }))

    checkbox.children.forEachIndexed { index, child ->
        val element = removeAt(indexOfFirst { it == child })
        val targetIndex = toIndex + index + 1
        if (targetIndex >= size) {
            add(element)
        } else {
            add(toIndex + index + 1, element)
        }
    }
    toPositionOf.children.forEachIndexed { index, child ->
        add(indexOfFirst { it == toPositionOf } + index + 1, removeAt(indexOfFirst { it == child }))
    }
}

private fun MutableList<ViewTemplateCheckbox>.moveChild(
    child: ViewTemplateCheckbox,
    toPositionOf: ViewTemplateCheckbox
) {
    val fromIndex = indexOfFirst { it == child }
    val toIndex = indexOfFirst { it == toPositionOf }
    val isMovingUp = fromIndex > toIndex
    val toParent = findNewParentId(toPositionOf, isMovingUp, toIndex)
    add(toIndex, removeAt(fromIndex))
    val oldParentIndex = indexOfFirst { it.id == child.parentId }
    val oldParent = removeAt(oldParentIndex)
    add(oldParentIndex, oldParent.minusChildCheckbox(child))
    val newParentIndex = indexOfFirst { it.id == toParent }
    val withUpdatedParentId = child.withUpdatedParentId(toParent)
    val newLocalIndex = if (toPositionOf.isChild) {
        toIndex - newParentIndex - 1
    } else {
        if (isMovingUp) {
            null
        } else {
            0
        }
    }
    val newParent = removeAt(newParentIndex)
    add(
        newParentIndex,
        newParent.plusChildCheckbox(withUpdatedParentId, newLocalIndex)
    )
    removeAt(toIndex)
    add(toIndex, withUpdatedParentId)
}

private fun MutableList<ViewTemplateCheckbox>.findNewParentId(
    toIndexOf: ViewTemplateCheckbox,
    isMovingUp: Boolean,
    toIndex: Int
): TemplateCheckboxId? {
    return if (toIndexOf.isChild) {
        toIndexOf.parentId
    } else {
        if (isMovingUp) {
            val itemThatIsParentOrChildOfTargetParent = get(toIndex - 1)
            if (itemThatIsParentOrChildOfTargetParent.isChild) {
                itemThatIsParentOrChildOfTargetParent.parentId
            } else {
                itemThatIsParentOrChildOfTargetParent.id
            }
        } else {
            toIndexOf.id
        }
    }
}

fun renderList(checkboxes: MutableList<ViewTemplateCheckbox>): String {
    return checkboxes.joinToString("\n") { parent ->
        val formattedChildren = parent.children.joinToString("\n") {
            "|       ${it.title}"
        }
        "${parent.title}\n$formattedChildren"
    }
}
