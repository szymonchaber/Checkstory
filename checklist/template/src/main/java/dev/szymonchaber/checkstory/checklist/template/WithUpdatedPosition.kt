package dev.szymonchaber.checkstory.checklist.template

import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
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
    fromCheckbox: ViewTemplateCheckbox,
    toCheckbox: ViewTemplateCheckbox
) {
    val toIndex = indexOfFirst { it == toCheckbox }
    add(toIndex, removeAt(indexOfFirst { it == fromCheckbox }))

    fromCheckbox.children.forEachIndexed { index, child ->
        val element = removeAt(indexOfFirst { it == child })
        val targetIndex = toIndex + index + 1
        if (targetIndex >= size) {
            add(element)
        } else {
            add(toIndex + index + 1, element)
        }
    }
    toCheckbox.children.forEachIndexed { index, child ->
        add(indexOfFirst { it == toCheckbox } + index + 1, removeAt(indexOfFirst { it == child }))
    }
}

private fun MutableList<ViewTemplateCheckbox>.moveChild(
    fromCheckbox: ViewTemplateCheckbox,
    toCheckbox: ViewTemplateCheckbox
) {
    val fromIndex = indexOfFirst { it == fromCheckbox }
    val toIndex = indexOfFirst { it == toCheckbox }
    // TODO jak sub-task idzie w dół, to "to" jest docelowym parentem
    // TODO ale jak sub-task idzie w górę, to "to" jest jego obecnym parentem - wtedy index -1 żeby znaleźć parenta
    // TODO reszta tak samo
    val isMovingUp = fromIndex > toIndex
    val toParent = if (toCheckbox.isChild) {
        toCheckbox.parentId
    } else {
        if (isMovingUp) {
            val itemThatIsParentOrChildOfTargetParent = get(toIndex - 1)
            if (itemThatIsParentOrChildOfTargetParent.isChild) {
                itemThatIsParentOrChildOfTargetParent.parentId
            } else {
                itemThatIsParentOrChildOfTargetParent.id
            }
        } else {
            toCheckbox.id
        }
    }
    add(toIndex, removeAt(fromIndex))
    val oldParentIndex = indexOfFirst { it.id == fromCheckbox.parentId }
    val oldParent = removeAt(oldParentIndex)
    add(oldParentIndex, oldParent.minusChildCheckbox(fromCheckbox))
    val newParentIndex = indexOfFirst { it.id == toParent }
    val newParent = removeAt(newParentIndex)
    val withUpdatedParentId = fromCheckbox.withUpdatedParentId(toParent)
    val newLocalIndex = if (toCheckbox.isChild) {
        toIndex - newParentIndex - 1
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
    removeAt(toIndex)
    add(toIndex, withUpdatedParentId)
}

fun renderList(checkboxes: MutableList<ViewTemplateCheckbox>): String {
    return checkboxes.joinToString("\n") { parent ->
        val formattedChildren = parent.children.joinToString("\n") {
            "|       ${it.title}"
        }
        "${parent.title}\n$formattedChildren"
    }
}
