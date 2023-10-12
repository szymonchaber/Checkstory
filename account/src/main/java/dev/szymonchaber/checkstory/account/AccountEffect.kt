package dev.szymonchaber.checkstory.account

@Suppress("CanSealedSubClassBeObject")
sealed interface AccountEffect {

    class ShowLoginNetworkError : AccountEffect

    class ShowDataNotSynchronized : AccountEffect

    data class ExitWithAuthResult(val isSuccess: Boolean) : AccountEffect

    class StartAuthUi : AccountEffect
}
