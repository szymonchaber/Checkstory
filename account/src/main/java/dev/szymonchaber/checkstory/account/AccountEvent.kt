package dev.szymonchaber.checkstory.account

import com.firebase.ui.auth.IdpResponse

sealed interface AccountEvent {

    data object LoadAccount : AccountEvent

    data object TriggerPartialRegistration : AccountEvent

    data object TriggerSignIn : AccountEvent

    data object LogoutClicked : AccountEvent

    data object LogoutDespiteUnsynchronizedDataClicked : AccountEvent

    data class FirebaseResultReceived(val response: IdpResponse) : AccountEvent

    data object LoginClicked : AccountEvent

    data object SignUpClicked : AccountEvent

    data object ManageSubscriptionsClicked : AccountEvent

    data object DeleteAccountClicked : AccountEvent
}
