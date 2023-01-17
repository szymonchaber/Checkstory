package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import org.burnoutcrew.reorderable.ItemPosition

fun withUpdatedPosition(
    checkboxes: List<ViewTemplateCheckbox>,
    from: ItemPosition,
    to: ItemPosition,
    eventCollector: (EditTemplateEvent) -> Unit = {}
): List<ViewTemplateCheckbox> {
    return checkboxes.toMutableList()
        .apply {
            val fromCheckbox = from.checkbox!!
            val toCheckbox = to.checkbox!!
            if (areParentsMoving(fromCheckbox, toCheckbox)) {
                eventCollector(EditTemplateEvent.ParentItemsSwapped(fromCheckbox, toCheckbox))
                moveParent(fromCheckbox, toCheckbox)
            }
            if (isChildMoving(fromCheckbox)) {
                moveChild(fromCheckbox, toCheckbox, eventCollector)
            }
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
    toPositionOf: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    val fromIndex = indexOfFirst { it == child }
    val toIndex = indexOfFirst { it == toPositionOf }
    val isMovingUp = fromIndex > toIndex
    val (oldParentIndex, oldParent) = findClosestParentBelow(fromIndex)
    replace(oldParentIndex, oldParent.minusChildCheckbox(child))
    add(toIndex, removeAt(fromIndex))
    val (newParentIndex, newParent) = findClosestParentBelow(toIndex)
    val newLocalIndex = if (toPositionOf.isChild) {
        toIndex - newParentIndex - 1
    } else {
        if (isMovingUp) {
            null
        } else {
            0
        }
    }
    replace(newParentIndex, newParent.plusChildCheckbox(child, newLocalIndex))
    eventCollector(EditTemplateEvent.ChildItemMoved(child, oldParent, newParent, newLocalIndex))
}

fun MutableList<ViewTemplateCheckbox>.findClosestParentBelow(toIndex: Int): Pair<Int, ViewTemplateCheckbox> {
    for (index in toIndex downTo 0) {
        val candidate = get(index)
        if (candidate.isParent) {
            return index to candidate
        }
    }
    error("No parents between indexes 0 & $toIndex")
}

fun MutableList<ViewTemplateCheckbox>.replace(index: Int, with: ViewTemplateCheckbox) {
    removeAt(index)
    add(index, with)
}
