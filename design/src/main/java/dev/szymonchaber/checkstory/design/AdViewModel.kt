package dev.szymonchaber.checkstory.design

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(getUserUseCase: GetUserUseCase) : ViewModel() {

    val shouldEnableAds = getUserUseCase.getUser().map { !it.isPaidUser }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}