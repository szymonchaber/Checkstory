package dev.szymonchaber.checkstory.account

data class AccountState(
    val accountLoadingState: AccountLoadingState,
    val authForPaymentRequested: Boolean,
    val purchaseRestorationOngoing: Boolean = false
) {

    companion object {

        val initial: AccountState = AccountState(AccountLoadingState.Loading, false)
    }
}
