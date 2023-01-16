package dev.szymonchaber.checkstory.common.extensions

fun <T, ID> List<T>.update(id: ID, idSelector: (T) -> ID, updater: (T) -> T): List<T> {
    var found = false
    return map {
        if (idSelector(it) == id) {
            found = true
            updater(it)
        } else {
            it
        }
    }.also {
        if (!found) {
            throw IllegalArgumentException("Did not find id to update: $id")
        }
    }
}
