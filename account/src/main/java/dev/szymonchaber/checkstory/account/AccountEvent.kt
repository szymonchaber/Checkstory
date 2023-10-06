package dev.szymonchaber.checkstory.account

sealed class AccountEvent {

    object LoadAccount : AccountEvent()

    data class LoginClicked(val email: String, val password: String) : AccountEvent()

    data class RegisterClicked(val email: String, val password: String) : AccountEvent()

    object LogoutClicked : AccountEvent()

    object LogoutDespiteUnsynchronizedDataClicked : AccountEvent()
}
