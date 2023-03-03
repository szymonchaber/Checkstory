package dev.szymonchaber.checkstory.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OnboardingPreferences @Inject constructor(
    @Named("onboardingPreferences")
    private val onboardingPreferencesDataStore: DataStore<Preferences>
) {

    val didShowOnboarding: Flow<Boolean> = onboardingPreferencesDataStore.data
        .map {
            it[KEY_DID_SHOW_ONBOARDING] ?: false
        }

    suspend fun updateDidShowOnboardingFlow(didShowOnboardingFlow: Boolean) {
        onboardingPreferencesDataStore.edit { preferences ->
            preferences[KEY_DID_SHOW_ONBOARDING] = didShowOnboardingFlow
        }
    }

    companion object {

        private val KEY_DID_SHOW_ONBOARDING = booleanPreferencesKey("DID_SHOW_ONBOARDING")
    }
}
