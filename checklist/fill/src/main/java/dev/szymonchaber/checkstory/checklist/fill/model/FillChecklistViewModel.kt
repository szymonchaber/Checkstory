package dev.szymonchaber.checkstory.checklist.fill.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class FillChecklistViewModel @Inject constructor() : BaseViewModel<
        FillChecklistEvent,
        FillChecklistState,
        FillChecklistEffect>(
    FillChecklistState(
        checklist.copy()
    )
) {

    override fun buildMviFlow(eventFlow: Flow<FillChecklistEvent>): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        val handleNothing: Flow<Pair<FillChecklistState, FillChecklistEffect?>> =
            eventFlow.filterIsInstance<FillChecklistEvent.Nothing>()
                .map {
                    FillChecklistState(checklist) to null
                }
        return merge(handleNothing)
    }
}
