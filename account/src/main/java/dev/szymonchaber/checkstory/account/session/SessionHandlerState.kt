package dev.szymonchaber.checkstory.account.session

internal data class SessionHandlerState(
    val showSessionExpiredDialog: Boolean = false,
    val showUnsynchronizedDataDialog: Boolean = false
)
