package dev.szymonchaber.checkstory.common.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel<EVENT, STATE, EFFECT>(initialState: STATE) : ViewModel() {

    protected val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EFFECT>(extraBufferCapacity = Int.MAX_VALUE)
    val effect: SharedFlow<EFFECT>
        get() = _effect.asSharedFlow()

    private val event: MutableSharedFlow<EVENT> = MutableSharedFlow(extraBufferCapacity = Int.MAX_VALUE)

    init {
        // TODO This could be done with a function init for "StateDelegate" instead of getting hung up on not having this field
        viewModelScope.launch {
            buildMviFlow(event)
                .collect { (state, effect) ->
                    state?.let { newState ->
                        _state.update {
                            newState
                        }
                    }
                    effect?.let {
                        Timber.d("Sending effect: $effect")
                        _effect.emit(effect)
                    }
                }
        }
    }

    abstract fun buildMviFlow(eventFlow: Flow<EVENT>): Flow<Pair<STATE?, EFFECT?>>

    fun onEvent(event: EVENT) {
        viewModelScope.launch {
            this@BaseViewModel.event.emit(event)
        }
    }
}
