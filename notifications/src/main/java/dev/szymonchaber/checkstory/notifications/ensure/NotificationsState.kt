package dev.szymonchaber.checkstory.notifications.ensure

internal data class NotificationsState(
    val showPermissionMissingDialog: Boolean = false,
    val didUserDismissWarning: Boolean = false
)
