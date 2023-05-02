package dev.szymonchaber.checkstory.account

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val tracker: Tracker
) : BaseViewModel<AccountEvent, AccountState, AccountEffect>(
    AccountState.initial
) {

    private val auth by lazy { Firebase.auth }

    override fun buildMviFlow(eventFlow: Flow<AccountEvent>): Flow<Pair<AccountState?, AccountEffect?>> {
        return merge(
            eventFlow.handleLoadAccount(),
            eventFlow.handleLoginSuccess()
        )
    }

    private fun Flow<AccountEvent>.handleLoadAccount(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoadAccount>()
            .flatMapLatest {
                val email = auth.currentUser?.email
                flowOf(AccountState(AccountLoadingState.Success(email)) to null)
            }
    }

    private fun Flow<AccountEvent>.handleLoginSuccess(): Flow<Pair<AccountState, AccountEffect?>> {
        return filterIsInstance<AccountEvent.LoginSuccess>()
            .map {
                AccountState(AccountLoadingState.Success("Logged in!")) to null
            }
    }
}

sealed class AccountEvent {

    object LoginSuccess : AccountEvent()

    object LoadAccount : AccountEvent()

    object ConfirmExitClicked : AccountEvent()

    object LoginFailed : AccountEvent()
}

data class AccountState(val accountLoadingState: AccountLoadingState) {

    companion object {

        val initial: AccountState = AccountState(AccountLoadingState.Loading)
    }
}

sealed interface AccountLoadingState {

    data class Success(val email: String?) : AccountLoadingState

    object Loading : AccountLoadingState
}

sealed interface AccountEffect
