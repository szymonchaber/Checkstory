package dev.szymonchaber.checkstory.notifications.ensure

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Composable
fun NotificationsEnsurer() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NotificationsEnsurerActual()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationsEnsurerActual() {
    val viewModel = hiltViewModel<NotificationsViewModel>()
    val state = viewModel.state.collectAsState()
    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val context = LocalContext.current
    if (state.value.checkNotificationPermissionMissing && !permissionState.status.isGranted) {
        NotificationPermissionDialog(
            onDismiss = viewModel::onUserDismissed,
            onGrantPermission = {
                if (permissionState.status.shouldShowRationale) {
                    launchNotificationSettingsIntent(context)
                } else {
                    permissionState.launchPermissionRequest()
                }
            },
        )
    }
}

private fun launchNotificationSettingsIntent(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }
}

@Composable
private fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onGrantPermission: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("Your reminders are at risk")
        },
        text = {
            Text("You have reminders set up, but the app is not allowed to show notifications. Please enable notifications to keep receiving reminders.")
        },
        confirmButton = {
            TextButton(onClick = onGrantPermission) {
                Text("Enable notifications")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ignore for now")
            }
        }
    )
}
