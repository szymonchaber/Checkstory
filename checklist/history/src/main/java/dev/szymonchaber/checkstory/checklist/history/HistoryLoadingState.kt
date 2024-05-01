package dev.szymonchaber.checkstory.checklist.history

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template

sealed interface HistoryLoadingState {

    object Loading : HistoryLoadingState

    data class Success(val template: Template) : HistoryLoadingState
}
