package dev.szymonchaber.checkstory.api.auth.model

import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiUser(val userId: String, val email: String?, val isPaidUser: Boolean) {

    fun toUser(): User.LoggedIn {
        val tier = if (isPaidUser) Tier.PAID else Tier.FREE
        return User.LoggedIn(
            id = userId,
            email = email,
            tier = tier
        )
    }
}
