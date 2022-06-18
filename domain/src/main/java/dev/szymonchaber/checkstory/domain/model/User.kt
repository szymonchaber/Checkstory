package dev.szymonchaber.checkstory.domain.model

sealed interface User {

    object Guest : User

    data class LoggedIn(val id: UserId, val tier: Tier) : User

    val isPaidUser: Boolean
        get() = this is LoggedIn && tier == Tier.PAID
}
