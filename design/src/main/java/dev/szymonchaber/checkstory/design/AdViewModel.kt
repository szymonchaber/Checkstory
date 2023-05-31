package dev.szymonchaber.checkstory.design

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(getCurrentUserUseCase: GetCurrentUserUseCase) : ViewModel() {

    val currentUserFlow = getCurrentUserUseCase.getCurrentUserFlow()
}
