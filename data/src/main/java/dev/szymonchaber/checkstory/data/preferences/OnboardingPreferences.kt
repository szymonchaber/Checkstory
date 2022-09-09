package dev.szymonchaber.checkstory.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OnboardingPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val preferences = context.getSharedPreferences(ONBOARDING_PREFERENCES, Context.MODE_PRIVATE)

    var didGenerateOnboardingTemplate: Boolean
        get() = preferences.getBoolean(KEY_ONBOARDING_TEMPLATE_GENERATED, false)
        set(value) = preferences.edit().putBoolean(KEY_ONBOARDING_TEMPLATE_GENERATED, value).apply()

    companion object {

        private const val ONBOARDING_PREFERENCES = "ONBOARDING_PREFERENCES"
        private const val KEY_ONBOARDING_TEMPLATE_GENERATED = "ONBOARDING_PREFERENCES"

    }
}
