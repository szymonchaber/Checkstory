package dev.szymonchaber.checkstory.design

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(getUserUseCase: GetUserUseCase) : ViewModel() {

    val shouldEnableAds = getUserUseCase.getUser().map { !it.isPaidUser }
}