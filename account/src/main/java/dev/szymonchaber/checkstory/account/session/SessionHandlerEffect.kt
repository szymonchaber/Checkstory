package dev.szymonchaber.checkstory.account.session

internal sealed interface SessionHandlerEffect {

    data object ShowLogoutSuccess : SessionHandlerEffect
}
