package dev.szymonchaber.checkstory.account

data class AccountState(val accountLoadingState: AccountLoadingState) {

    companion object {

        val initial: AccountState = AccountState(AccountLoadingState.Loading)
    }
}
