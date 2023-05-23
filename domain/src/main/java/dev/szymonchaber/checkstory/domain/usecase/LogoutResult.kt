package dev.szymonchaber.checkstory.domain.usecase

sealed interface LogoutResult {

    object Done : LogoutResult

    object UnsynchronizedCommandsPresent : LogoutResult
}
