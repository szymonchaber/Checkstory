package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.model.renderCheckboxes
import timber.log.Timber

fun wrapReorderChanges(
    viewTemplateCheckboxes: List<ViewTemplateCheckbox>,
    from: ViewTemplateCheckboxKey,
    to: ViewTemplateCheckboxKey
): List<ViewTemplateCheckbox> {
//    Timber.d("########## Reordering from: ${from.title} to: ${to.title}")
//    println("########## Reordering from: ${from.title} to: ${to.title}")
    val newList = withUpdatedPosition(viewTemplateCheckboxes, from, to)
    return wrap(newList)
}

private fun wrap(unwrappedCheckboxes: List<ViewTemplateCheckbox>): List<ViewTemplateCheckbox> {
    Timber.d("Wrapping:\n${renderCheckboxes(unwrappedCheckboxes)}")
    println("Wrapping:\n${renderCheckboxes(unwrappedCheckboxes)}")
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
    return checkboxes.toMutableList()
        .apply {
            if (areParentsMoving(fromCheckbox, toCheckbox)) {
                moveParent(fromCheckbox, toCheckbox)
            }
            if (isChildMoving(fromCheckbox)) {
                moveChild(fromCheckbox, toCheckbox)
            }
        }.also {
            println("Reordered: " + it.map { it.title })
        }
}

private fun isChildMoving(fromCheckbox: ViewTemplateCheckboxKey) = fromCheckbox.isChild

private fun areParentsMoving(fromCheckbox: ViewTemplateCheckboxKey, toCheckbox: ViewTemplateCheckboxKey): Boolean {
    return fromCheckbox.isParent && toCheckbox.isParent
}

private fun MutableList<ViewTemplateCheckbox>.moveParent(
    checkbox: ViewTemplateCheckboxKey,
    toPositionOf: ViewTemplateCheckboxKey
) {
    var fromIndex = indexOfFirst { it.viewKey == checkbox }

    val sourceChildrenCuttof = findNextParentIndex(fromIndex) ?: size
    val sourceChildrenIndices = fromIndex + 1 until sourceChildrenCuttof
    val sourceChildren = sourceChildrenIndices.mapIndexed { index, indice ->
        removeAt(indice - index)
    }
    println("Source: $checkbox\nSource children:\n${renderCheckboxes(sourceChildren)}\n-------")

    var toIndex = indexOfFirst { it.viewKey == toPositionOf }

    val targetChildrenCutoff = findNextParentIndex(toIndex) ?: size
    val targetChildrenIndices = toIndex + 1 until targetChildrenCutoff
    val targetChildren = targetChildrenIndices.mapIndexed { index, indice ->
        removeAt(indice - index)
    }
    println("Target: $toPositionOf\nTarget children:\n${renderCheckboxes(targetChildren)}\n-------")

    toIndex = indexOfFirst { it.viewKey == toPositionOf }
    fromIndex = indexOfFirst { it.viewKey == checkbox }

    add(toIndex, removeAt(fromIndex))

    sourceChildren.forEachIndexed { index, child ->
        val targetIndex = indexOfFirst { it.viewKey == checkbox } + index + 1
        if (targetIndex >= size) {
            add(child)
        } else {
            add(targetIndex, child)
        }
    }
    targetChildren.forEachIndexed { index, child ->
        val targetIndex = indexOfFirst { it.viewKey == toPositionOf } + index + 1
        if (targetIndex >= size) {
            add(child)
        } else {
            add(targetIndex, child)
        }
    }
}

private fun MutableList<ViewTemplateCheckbox>.moveChild(
    child: ViewTemplateCheckboxKey,
    toPositionOf: ViewTemplateCheckboxKey
) {
    val fromIndex = indexOfFirst { it.viewKey == child }
    val toIndex = indexOfFirst { it.viewKey == toPositionOf }
    add(toIndex, removeAt(fromIndex))
}

fun MutableList<ViewTemplateCheckbox>.findNextParentIndex(fromIndex: Int): Int? {
    for (index in fromIndex + 1 until size) {
        val candidate = get(index)
        Timber.e("Candidate: ${candidate.title}")
        if (candidate.isParent) {
            return index
        }
    }
    return null
}
