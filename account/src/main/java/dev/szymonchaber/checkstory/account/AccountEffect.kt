package dev.szymonchaber.checkstory.account

sealed interface AccountEffect {

    object ShowLoginNetworkError : AccountEffect

    object ShowDataNotSynchronized : AccountEffect
}
