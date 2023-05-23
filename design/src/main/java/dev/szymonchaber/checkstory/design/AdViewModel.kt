package dev.szymonchaber.checkstory.design

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(getCurrentUserUseCase: GetCurrentUserUseCase) : ViewModel() {

    var shouldEnableAds: Boolean = false

    val currentUserFlow = getCurrentUserUseCase.getCurrentUserFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getCurrentUserUseCase.getCurrentUserFlow()
                .map { !it.isPaidUser }
                .onEach {
                    shouldEnableAds = it
                }
                .collect()
        }
    }
}
