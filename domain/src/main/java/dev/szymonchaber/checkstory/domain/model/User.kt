package dev.szymonchaber.checkstory.domain.model

sealed interface User {

    object Guest : User

    data class LoggedIn(val tier: Tier) : User

    val isPaidUser: Boolean
        get() = this is LoggedIn && tier == Tier.PAID

    val isLoggedIn: Boolean
        get() = this is LoggedIn
}
