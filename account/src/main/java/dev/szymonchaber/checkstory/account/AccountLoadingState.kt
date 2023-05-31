package dev.szymonchaber.checkstory.account

import dev.szymonchaber.checkstory.domain.model.User

sealed interface AccountLoadingState {

    data class Success(val user: User) : AccountLoadingState

    object Loading : AccountLoadingState
}
