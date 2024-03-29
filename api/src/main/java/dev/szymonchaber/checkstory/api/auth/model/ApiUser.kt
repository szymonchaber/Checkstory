package dev.szymonchaber.checkstory.api.auth.model

import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiUser(val userId: String, val isPaidUser: Boolean) {

    fun toUser(): User {
        return User.LoggedIn(if (isPaidUser) Tier.PAID else Tier.FREE)
    }
}
