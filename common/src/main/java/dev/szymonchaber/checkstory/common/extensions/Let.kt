package dev.szymonchaber.checkstory.common.extensions

fun <T, U, R> let(first: T?, second: U?, block: (T, U) -> R): R? {
    return first?.let { nonNullFirst ->
        second?.let { nonNullSecond ->
            block(nonNullFirst, nonNullSecond)
        }
    }
}
