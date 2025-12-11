package pt.ipp.estg.trabalho_cmu.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.notifications.NotificationManager

/**
 * Dialog to request notification permission from user.
 *
 */
@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onPermissionResult: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onPermissionResult(granted)
            if (granted) {
                onDismiss()
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.notification_permission_title),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = stringResource(R.string.notification_permission_message),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Android < 13, no permission needed
                        onPermissionResult(true)
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.notification_permission_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.notification_permission_skip))
            }
        }
    )
}

/**
 * Helper function to check if we should show the permission dialog.
 *
 * Show the dialog if:
 * - Android 13+ (permission required)
 * - Permission not yet granted
 * - User hasn't dismissed it before (optional, can track with SharedPreferences)
 */
@Composable
fun shouldShowNotificationPermissionDialog(): Boolean {
    val context = LocalContext.current

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        !NotificationManager.hasNotificationPermission(context)
    } else {
        false
    }
}

/**
 * Composable that automatically shows notification permission dialog on first launch.
 *
 * Usage in MainActivity or main screen:
 * ```
 * NotificationPermissionHandler()
 * ```
 */
@Composable
fun NotificationPermissionHandler() {
    val shouldShow = shouldShowNotificationPermissionDialog()
    var showDialog by remember { mutableStateOf(shouldShow) }

    LaunchedEffect(shouldShow) {
        showDialog = shouldShow
    }

    if (showDialog) {
        NotificationPermissionDialog(
            onDismiss = { showDialog = false },
            onPermissionResult = { granted ->
                if (granted) {
                } else {
                }
            }
        )
    }
}