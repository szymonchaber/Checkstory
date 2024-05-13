package dev.szymonchaber.checkstory.api.auth.model

import kotlinx.serialization.Serializable

@Serializable
internal data class RegisterPayload(
    val email: String?
)
