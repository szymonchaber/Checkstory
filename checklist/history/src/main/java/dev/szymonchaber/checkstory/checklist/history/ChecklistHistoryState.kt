package dev.szymonchaber.checkstory.checklist.history

data class ChecklistHistoryState(
    val loadingState: HistoryLoadingState = HistoryLoadingState.Loading
) {

    companion object {

        val initial = ChecklistHistoryState()
    }
}
