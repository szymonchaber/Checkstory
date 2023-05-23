package dev.szymonchaber.checkstory.account

sealed class AccountEvent {

    object LogoutClicked : AccountEvent()

    object LogoutDespiteUnsynchronizedDataClicked : AccountEvent()

    object LoginSuccess : AccountEvent()

    object LoadAccount : AccountEvent()

    object LoginFailed : AccountEvent()
}
