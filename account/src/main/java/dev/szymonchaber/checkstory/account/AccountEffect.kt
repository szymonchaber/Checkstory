package dev.szymonchaber.checkstory.account

sealed interface AccountEffect {

    data object ShowLoginNetworkError : AccountEffect

    data object ShowDataNotSynchronized : AccountEffect

    data class ExitWithAuthResult(val isSuccess: Boolean) : AccountEffect

    data object StartAuthUi : AccountEffect

    data object NavigateToPurchaseScreen : AccountEffect

    data object NavigateToSubscriptionManagement : AccountEffect
}
