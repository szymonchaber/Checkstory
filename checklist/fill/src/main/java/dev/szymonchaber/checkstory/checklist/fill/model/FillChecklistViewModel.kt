package dev.szymonchaber.checkstory.checklist.fill.model

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FillChecklistViewModel @Inject constructor() :
    BaseViewModel<FillChecklistEvent, FillChecklistState, FillChecklistEffect>(
        FillChecklistState(checklist.copy())
    ) {

    override fun buildMviFlow(eventFlow: Flow<FillChecklistEvent>): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        val handleCheckChanged = eventFlow
            .onEach {
                Log.d("TAG", "Event: $it")
            }.handleCheckChanged()
        return merge(handleCheckChanged)
    }

    private fun Flow<FillChecklistEvent>.handleCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.CheckChanged>()
            .map { checkChanged ->
                val state = state.first()
                val checklist = state.checklist
                val updatedList = state.checklist.items.map {
                    if (it == checkChanged.item) {
                        it.copy(isChecked = checkChanged.newCheck)
                    } else {
                        it
                    }
                }
                FillChecklistState(checklist.copy(items = updatedList)) to null
            }
    }
}
