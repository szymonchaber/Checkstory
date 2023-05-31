package dev.szymonchaber.checkstory.account

@Suppress("CanSealedSubClassBeObject")
sealed interface AccountEffect {

    class ShowLoginNetworkError : AccountEffect

    class ShowDataNotSynchronized : AccountEffect
}
