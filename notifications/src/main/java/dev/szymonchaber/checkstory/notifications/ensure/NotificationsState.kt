package dev.szymonchaber.checkstory.notifications.ensure

internal data class NotificationsState(
    val checkNotificationPermissionMissing: Boolean = false,
    val didUserDismissWarning: Boolean = false
)
