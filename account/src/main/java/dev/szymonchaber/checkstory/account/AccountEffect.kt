package dev.szymonchaber.checkstory.account

sealed interface AccountEffect {

    data object ShowLoginNetworkError : AccountEffect

    data object ShowDataNotSynchronized : AccountEffect

    data class ExitWithAuthResult(val isSuccess: Boolean) : AccountEffect

    data class StartAuthUi(val allowNewAccounts: Boolean) : AccountEffect

    data object NavigateToPurchaseScreen : AccountEffect

    data object NavigateToSubscriptionManagement : AccountEffect

    data object ShowNoPurchasesFound : AccountEffect

    data object ShowPurchaseRestored : AccountEffect
}
