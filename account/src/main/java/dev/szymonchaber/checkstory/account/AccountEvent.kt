package dev.szymonchaber.checkstory.account

sealed class AccountEvent {

    object LoadAccount : AccountEvent()

    object LoginClicked : AccountEvent()

    object LogoutClicked : AccountEvent()

    object LogoutDespiteUnsynchronizedDataClicked : AccountEvent()
}
