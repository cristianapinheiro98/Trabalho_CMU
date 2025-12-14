package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pt.ipp.estg.trabalho_cmu.R

/**
 * Dialog displayed when user tries to access SocialTails Community while offline.
 *
 * Informs the user that an internet connection is required to view
 * community content and provides a button to dismiss and go back.
 *
 * @param onDismiss Callback when user dismisses the dialog
 */
@Composable
fun OfflineDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null
            )
        },
        title = {
            Text(text = stringResource(R.string.offline_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.offline_dialog_message))
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.offline_dialog_button))
            }
        }
    )
}