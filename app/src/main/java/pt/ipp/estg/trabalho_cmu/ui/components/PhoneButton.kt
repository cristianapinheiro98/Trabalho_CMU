package pt.ipp.estg.trabalho_cmu.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

/**
 * A reusable button that opens the phone dialer with the provided phone number.
 *
 * Features:
 * - Displays a green button with a phone icon
 * - Uses localized text and description
 * - Automatically creates and launches a dial Intent
 *
 * @param phoneNumber The number that will be pre-filled in the dialer.
 * @param modifier Modifier to control button layout.
 * @param buttonColor Allows customizing the button background color.
 * @param text Optional override for button text (default uses stringResource).
 */
@Composable
fun PhoneButton(
    phoneNumber: String,
    modifier: Modifier = Modifier,
    buttonColor: Color = Color(0xFF4CAF50),
    text: String = stringResource(R.string.call_button_label)
) {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = stringResource(R.string.phone_icon_description),
            tint = Color.White
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
