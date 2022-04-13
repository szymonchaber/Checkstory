package dev.szymonchaber.checkstory.data.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

internal fun <T> List<Flow<T>>.toFlowOfLists(): Flow<List<T>> {
    return fold(flowOf(listOf())) { itemListFlow, itemFlow ->
        itemListFlow.combine(itemFlow) { left, right ->
            left.plus(right)
        }
    }
}
