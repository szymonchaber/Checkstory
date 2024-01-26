package dev.szymonchaber.checkstory.account

import com.firebase.ui.auth.IdpResponse

sealed class AccountEvent {

    data object LoadAccount : AccountEvent()

    data object TriggerPartialRegistration : AccountEvent()

    data object LogoutClicked : AccountEvent()

    data object LogoutDespiteUnsynchronizedDataClicked : AccountEvent()

    data class FirebaseResultReceived(val response: IdpResponse) : AccountEvent()

    data object FirebaseLoginClicked : AccountEvent()
}
