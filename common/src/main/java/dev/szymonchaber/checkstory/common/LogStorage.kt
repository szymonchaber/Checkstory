package dev.szymonchaber.checkstory.common

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogStorage @Inject constructor() {

    val logState = MutableStateFlow("")

    fun append(line: String) {
        logState.tryEmit(logState.value + line + "\n\n")
    }
}
