package dev.szymonchaber.checkstory.account

sealed interface AccountEffect {

    data object ShowLoginNetworkError : AccountEffect

    data object ShowDataNotSynchronized : AccountEffect

    data class ExitWithAuthResult(val isSuccess: Boolean, val loggedInEmail: String?) : AccountEffect

    data object StartAuthUi : AccountEffect

    data object NavigateToPurchaseScreen : AccountEffect

    data object ShowConfirmDeleteAccountDialog : AccountEffect

    data object NavigateToSubscriptionManagement : AccountEffect

    data object ShowNoPurchasesFound : AccountEffect

    data object ShowPurchaseRestored : AccountEffect

    data object ShowPurchaseRestorationFailed : AccountEffect

    data object ShowPurchaseAssignedToAnotherUser : AccountEffect

    data object ShowAccountDeleted : AccountEffect
}
