package dev.szymonchaber.checkstory.common.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<EVENT, STATE, EFFECT>(initialState: STATE) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: Flow<STATE>
        get() = _state

    private val _effect = kotlinx.coroutines.channels.Channel<EFFECT>()
    val effect: Flow<EFFECT>
        get() = _effect.consumeAsFlow()

    private val event: MutableSharedFlow<EVENT> = MutableSharedFlow()

    init {
        viewModelScope.launch {
            buildMviFlow(event)
                .collect { (state, effect) ->
                    _state.emit(state)
                    effect?.let {
                        _effect.send(effect)
                    }
                }
        }
    }

    abstract fun buildMviFlow(eventFlow: Flow<EVENT>): Flow<Pair<STATE, EFFECT?>>

    fun onEvent(event: EVENT) {
        viewModelScope.launch {
            this@BaseViewModel.event.emit(event)
        }
    }
}
