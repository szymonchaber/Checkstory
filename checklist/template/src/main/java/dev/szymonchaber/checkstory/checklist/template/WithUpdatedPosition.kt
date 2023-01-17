package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox

fun wrapReorderChanges(
    viewTemplateCheckboxes: List<ViewTemplateCheckbox>,
    from: ViewTemplateCheckbox,
    to: ViewTemplateCheckbox
): List<ViewTemplateCheckbox> {
    val newList = withUpdatedPosition(viewTemplateCheckboxes, from, to)
    return wrap(newList)
}

private fun wrap(unwrappedCheckboxes: List<ViewTemplateCheckbox>): List<ViewTemplateCheckbox> {
    val parentsToChildren = mutableMapOf<ViewTemplateCheckbox, List<ViewTemplateCheckbox>>()
    var lastParent: ViewTemplateCheckbox? = null
    unwrappedCheckboxes.forEach {
        if (it.isParent) {
            lastParent = it
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
    fromCheckbox: ViewTemplateCheckbox,
    toCheckbox: ViewTemplateCheckbox
): List<ViewTemplateCheckbox> {
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
    add(toIndex, removeAt(fromIndex))
}
