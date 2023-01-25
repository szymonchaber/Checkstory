package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.model.renderCheckbox

fun wrapReorderChanges(
    viewTemplateCheckboxes: List<ViewTemplateCheckbox>,
    from: ViewTemplateCheckboxKey,
    to: ViewTemplateCheckboxKey
): List<ViewTemplateCheckbox> {
    return wrap(withUpdatedPosition(viewTemplateCheckboxes, from, to))
}

private fun wrap(unwrappedCheckboxes: List<ViewTemplateCheckbox>): List<ViewTemplateCheckbox> {
    val parentsToChildren = mutableMapOf<ViewTemplateCheckbox, List<ViewTemplateCheckbox>>()
    var lastParent: ViewTemplateCheckbox? = null
    unwrappedCheckboxes.forEach {
        if (it.isParent) {
            lastParent = it
            parentsToChildren.merge(it, listOf()) { old, new ->
                old + new
            }
        } else {
            parentsToChildren.merge(lastParent!!, listOf(it)) { old, new ->
                old + new
            }
        }
    }
    return parentsToChildren.map { (parent, children) ->
        parent.replaceChildren(children)
    }
}

fun withUpdatedPosition(
    checkboxes: List<ViewTemplateCheckbox>,
    fromCheckbox: ViewTemplateCheckboxKey,
    toCheckbox: ViewTemplateCheckboxKey
): List<ViewTemplateCheckbox> {
    require(fromCheckbox != toCheckbox) {
        "\"from\" and \"to\" checkbox keys are the same: $fromCheckbox"
    }
    return checkboxes.toMutableList()
        .apply {
            if (areParentsMoving(fromCheckbox, toCheckbox)) {
                moveParent(fromCheckbox, toCheckbox)
            }
            if (isChildMoving(fromCheckbox)) {
                moveChild(fromCheckbox, toCheckbox)
            }
        }
}

private fun MutableList<ViewTemplateCheckbox>.moveParent(
    fromCheckboxKey: ViewTemplateCheckboxKey,
    toCheckboxKey: ViewTemplateCheckboxKey
) {
    val fromIndex = indexOfFirst { it.viewKey == fromCheckboxKey }
    val toIndex = indexOfFirst { it.viewKey == toCheckboxKey }
    val isMovingUp = fromIndex > toIndex
    val sourceParentChildrenCount = findChildrenCount(fromIndex)
    if (isMovingUp) {
        add(toIndex, removeAt(fromIndex))
        repeat(sourceParentChildrenCount) {
            add(toIndex + 1, removeAt(fromIndex + sourceParentChildrenCount))
        }
    } else {
        val targetParentChildrenCount = findChildrenCount(toIndex)
        val offsetToIndex = toIndex + targetParentChildrenCount
        add(offsetToIndex, removeAt(fromIndex))
        repeat(sourceParentChildrenCount) {
            add(offsetToIndex, removeAt(fromIndex))
        }
    }
}

fun MutableList<ViewTemplateCheckbox>.findChildrenCount(afterIndex: Int): Int {
    return findNextParentIndex(afterIndex)?.minus(1)?.minus(afterIndex) ?: (size - afterIndex - 1)
}

private fun isChildMoving(fromCheckbox: ViewTemplateCheckboxKey) = fromCheckbox.isChild

private fun areParentsMoving(fromCheckbox: ViewTemplateCheckboxKey, toCheckbox: ViewTemplateCheckboxKey): Boolean {
    return fromCheckbox.isParent && toCheckbox.isParent
}

private fun MutableList<ViewTemplateCheckbox>.moveChild(
    child: ViewTemplateCheckboxKey,
    toPositionOf: ViewTemplateCheckboxKey
) {
    val fromIndex = indexOfFirst { it.viewKey == child }
    val toIndex = indexOfFirst { it.viewKey == toPositionOf }
    require(toIndex != 0) {
        "Attempting to move a child ${renderCheckbox(get(fromIndex))} to top-most position"
    }
    add(toIndex, removeAt(fromIndex))
}

fun MutableList<ViewTemplateCheckbox>.findNextParentIndex(fromIndex: Int): Int? {
    for (index in fromIndex + 1 until size) {
        val candidate = get(index)
        if (candidate.isParent) {
            return index
        }
    }
    return null
}
