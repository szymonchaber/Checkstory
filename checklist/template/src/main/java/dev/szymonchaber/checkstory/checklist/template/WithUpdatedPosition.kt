package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import org.burnoutcrew.reorderable.ItemPosition
import timber.log.Timber

fun withUpdatedPosition(
    checkboxes: List<ViewTemplateCheckbox>,
    to: ItemPosition,
    from: ItemPosition
): List<ViewTemplateCheckbox> {
    return checkboxes.toMutableList()
        .apply {
            Timber.d("Items before moving:\n${renderList(this)}")
            if (areParentsMoving(from, to)) {
                moveParent(from, to)
            }
            if (isChildMoving(from)) {
                moveChild(from, to)
            }
            Timber.d("Items after moving:\n${renderList(this)}")
        }
}

private fun isChildMoving(from: ItemPosition): Boolean {
    return from.checkbox?.isChild == true
}

private fun areParentsMoving(
    from: ItemPosition,
    to: ItemPosition
): Boolean {
    return from.checkbox?.isParent == true && to.checkbox?.isParent == true
}

private fun MutableList<ViewTemplateCheckbox>.moveParent(
    from: ItemPosition,
    to: ItemPosition
) {
    val fromCheckbox = from.checkbox!!
    val toCheckbox = to.checkbox!!
    val newIndex = indexOfFirst { it == toCheckbox }
    add(newIndex, removeAt(indexOfFirst { it == fromCheckbox }))

    fromCheckbox.children.forEachIndexed { index, child ->
        add(newIndex + index + 1, removeAt(indexOfFirst { it == child }))
    }
    toCheckbox.children.forEachIndexed { index, child ->
        add(indexOfFirst { it == toCheckbox } + index + 1, removeAt(indexOfFirst { it == child }))
    }
}

private fun MutableList<ViewTemplateCheckbox>.moveChild(
    from: ItemPosition,
    to: ItemPosition
) {
    val newIndex = indexOfFirst { it == to.key }
    // TODO jak sub-task idzie w dół, to "to" jest docelowym parentem
    // TODO ale jak sub-task idzie w górę, to "to" jest jego obecnym parentem - wtedy index -1 żeby znaleźć parenta
    // TODO reszta tak samo
    val fromCheckbox = from.checkbox!!
    val toCheckbox = to.checkbox!!
    val isMovingUp = from.index > to.index
    val toParent = if (toCheckbox.isChild) {
        toCheckbox.parentId
    } else {
        if (isMovingUp) {
            val itemThatIsParentOrChildOfTargetParent = get(to.index - 2) // TODO why the fuck 2?
            if (itemThatIsParentOrChildOfTargetParent.isChild) {
                itemThatIsParentOrChildOfTargetParent.parentId
            } else {
                itemThatIsParentOrChildOfTargetParent.id
            }
        } else {
            toCheckbox.id
        }
    }
    add(newIndex, removeAt(indexOfFirst { it == fromCheckbox }))
    val oldParentIndex = indexOfFirst { it.id == fromCheckbox.parentId }
    val oldParent = removeAt(oldParentIndex)
    add(oldParentIndex, oldParent.minusChildCheckbox(fromCheckbox))
    val newParentIndex = indexOfFirst { it.id == toParent }
    val newParent = removeAt(newParentIndex)
    val withUpdatedParentId = fromCheckbox.withUpdatedParentId(toParent)
    val newLocalIndex = if (toCheckbox.isChild) {
        newParent.children.indexOf(toCheckbox)
    } else {
        if (isMovingUp) {
            null
        } else {
            0
        }
    }
    add(
        newParentIndex,
        newParent.plusChildCheckbox(withUpdatedParentId, newLocalIndex)
    )
    removeAt(newIndex)
    add(newIndex, withUpdatedParentId)
}

fun renderList(checkboxes: MutableList<ViewTemplateCheckbox>): String {
    return checkboxes.joinToString("\n") { parent ->
        val formattedChildren = parent.children.joinToString("\n") {
            "|       ${it.title}"
        }
        "${parent.title}\n$formattedChildren"
    }
}
