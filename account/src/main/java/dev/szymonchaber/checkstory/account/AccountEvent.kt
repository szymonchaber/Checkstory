package dev.szymonchaber.checkstory.account

sealed class AccountEvent {

    object LoadAccount : AccountEvent()

    data class LoginClicked(val email: String) : AccountEvent()

    data class RegisterClicked(val email: String) : AccountEvent()

    object LogoutClicked : AccountEvent()

    object LogoutDespiteUnsynchronizedDataClicked : AccountEvent()
}
