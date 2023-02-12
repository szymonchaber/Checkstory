package dev.szymonchaber.checkstory.design

import androidx.compose.runtime.compositionLocalOf
import dev.szymonchaber.checkstory.domain.model.User

val ActiveUser = compositionLocalOf<User> { error("No active user found!") }
