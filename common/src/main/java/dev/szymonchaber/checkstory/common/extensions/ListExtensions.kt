package dev.szymonchaber.checkstory.common.extensions

fun <T, ID> List<T>.update(id: ID, idSelector: (T) -> ID, updater: (T) -> T): List<T> {
    return toMutableList().apply {
        val index = indexOfFirst { idSelector(it) == id }
        require(index != -1) {
            "Did not find id to update: $id"
        }
        add(index, removeAt(index).let(updater))
    }
}
