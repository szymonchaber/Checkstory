package dev.szymonchaber.checkstory.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.data.preferences.OnboardingPreferences
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    init {
        viewModelScope.launch {
            onboardingPreferences.updateDidShowOnboardingFlow(true)
        }
    }
}
