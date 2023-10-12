package dev.szymonchaber.checkstory.account

data class AccountState(val accountLoadingState: AccountLoadingState, val partialAuthRequested: Boolean) {

    companion object {

        val initial: AccountState = AccountState(AccountLoadingState.Loading, false)
    }
}
