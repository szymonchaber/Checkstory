package dev.szymonchaber.checkstory.design

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(getUserUseCase: GetUserUseCase) : ViewModel() {

    var shouldEnableAds: Boolean = false

    val currentUserFlow = getUserUseCase.getUser()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getUserUseCase.getUser()
                .map { !it.isPaidUser }
                .onEach {
                    shouldEnableAds = it
                }
                .collect()
        }
    }
}
