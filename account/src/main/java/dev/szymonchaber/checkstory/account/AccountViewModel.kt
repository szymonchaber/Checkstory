package dev.szymonchaber.checkstory.account

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val tracker: Tracker,
    private val repository: ChecklistTemplateRepository
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
            .flatMapLatest {
                flow {
                    val email = auth.currentUser?.email
                    val token = suspendCoroutine { continuation ->
                        auth.currentUser?.getIdToken(false)?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                continuation.resumeWith(Result.success(it.result.token!!))
                            }
                        }
                    }
                    emit(AccountState(AccountLoadingState.Success(getServerResponse(token))) to null)
                }
            }
    }

    private suspend fun getServerResponse(token: String): String {
        return repository.helloWorld(token)
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<AccountLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.accountLoadingState }
                .filterIsInstance<AccountLoadingState.Success>()
                .map { it to event }
                .take(1)
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
