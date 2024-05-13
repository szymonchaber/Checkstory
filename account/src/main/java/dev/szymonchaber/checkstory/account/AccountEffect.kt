package dev.szymonchaber.checkstory.account

sealed interface AccountEffect {

    data object ShowLoginNetworkError : AccountEffect

    data object ShowDataNotSynchronized : AccountEffect

    data class ExitWithAuthResult(val isSuccess: Boolean) : AccountEffect

    data object StartAuthUi : AccountEffect

    data object NavigateToPurchaseScreen : AccountEffect

    data object NavigateToSubscriptionManagement : AccountEffect

    data object ShowNoPurchasesFound : AccountEffect

    data object ShowPurchaseRestored : AccountEffect

    data object NavigateBack : AccountEffect

    data object ShowPurchaseRestorationFailed : AccountEffect

    data object ShowPurchaseAssignedToAnotherUser : AccountEffect
}
