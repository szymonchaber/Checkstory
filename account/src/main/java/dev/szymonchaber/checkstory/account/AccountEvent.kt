package dev.szymonchaber.checkstory.account

import com.firebase.ui.auth.IdpResponse

sealed class AccountEvent {

    object LoadAccount : AccountEvent()

    data class LoginClicked(val email: String, val password: String) : AccountEvent()

    data class RegisterClicked(val email: String, val password: String) : AccountEvent()

    object LogoutClicked : AccountEvent()

    object LogoutDespiteUnsynchronizedDataClicked : AccountEvent()

    data class FirebaseResultReceived(val response: IdpResponse) : AccountEvent()
}
