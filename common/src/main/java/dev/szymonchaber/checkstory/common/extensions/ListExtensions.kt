package dev.szymonchaber.checkstory.common.extensions

fun <T, ID> List<T>.update(id: ID, idSelector: (T) -> ID, updater: (T) -> T): List<T> {
    return map {
        if (idSelector(it) == id) {
            updater(it)
        } else {
            it
        }
    }
}
