package dev.szymonchaber.checkstory.account.session

import com.firebase.ui.auth.IdpResponse

internal sealed interface SessionHandlerEvent {

    data class FirebaseLoginStateChanged(val firebaseLoggedIn: Boolean) : SessionHandlerEvent

    data class FirebaseAuthResultReceived(val idpResponse: IdpResponse) : SessionHandlerEvent

    data object LogoutClicked : SessionHandlerEvent

    data object LoginClicked : SessionHandlerEvent

    data object LogoutDespiteUnsynchronizedDataClicked : SessionHandlerEvent

    data object LogoutCancelled : SessionHandlerEvent

    data object TryAgainClicked : SessionHandlerEvent
}
