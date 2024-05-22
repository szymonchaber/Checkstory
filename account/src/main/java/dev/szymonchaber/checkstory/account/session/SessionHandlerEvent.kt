package dev.szymonchaber.checkstory.account.session

internal sealed interface SessionHandlerEvent {

    data class FirebaseLoginStateChanged(val firebaseLoggedIn: Boolean) : SessionHandlerEvent

    data object LogoutClicked : SessionHandlerEvent

    data object LogoutDespiteUnsynchronizedDataClicked : SessionHandlerEvent

    data object LogoutCancelled : SessionHandlerEvent
}
