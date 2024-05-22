package dev.szymonchaber.checkstory.account.session

internal sealed interface SessionHandlerEffect {

    data object ShowLogoutSuccess : SessionHandlerEffect

    data object ShowUnknownError : SessionHandlerEffect

    data class ShowLoginSuccessful(val email: String?) : SessionHandlerEffect

    data class LaunchFirebaseAuth(val defaultEmail: String?) : SessionHandlerEffect
}
