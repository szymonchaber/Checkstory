package dev.szymonchaber.checkstory.domain.model

sealed interface User {

    data class Guest(
        val deviceHasLocalPayment: Boolean = false // TODO Remove this field & require registration for local payments at some later date
    ) : User

    data class LoggedIn(val tier: Tier) : User

    val isPaidUser: Boolean
        get() {
            return when (this) {
                is Guest -> this.deviceHasLocalPayment
                is LoggedIn -> this.tier == Tier.PAID
            }
        }

    val isLoggedIn: Boolean
        get() = this is LoggedIn
}
