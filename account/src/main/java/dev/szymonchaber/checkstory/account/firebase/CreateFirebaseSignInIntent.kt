package dev.szymonchaber.checkstory.account.firebase

import android.content.Intent
import com.firebase.ui.auth.AuthUI

internal fun createFirebaseSignInIntent(
    termsOfServiceUrl: String,
    privacyPolicyUrl: String,
    allowNewAccounts: Boolean = true,
    defaultEmail: String? = null
): Intent {
    return AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(
            listOf(
                AuthUI.IdpConfig.EmailBuilder()
                    .setRequireName(false)
                    .setAllowNewAccounts(allowNewAccounts)
                    .setDefaultEmail(defaultEmail)
                    .build()
            )
        )
        .setIsSmartLockEnabled(
            /* enableCredentials = */ false,
            /* enableHints = */ true
        )
//                            .setTheme(DesignR.style.Theme_Checkstory)
        .setTosAndPrivacyPolicyUrls(
            termsOfServiceUrl,
            privacyPolicyUrl,
        )
        .build()
}
