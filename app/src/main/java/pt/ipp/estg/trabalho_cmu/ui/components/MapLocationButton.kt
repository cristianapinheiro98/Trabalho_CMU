package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun MapLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2C8B7E),
            contentColor = Color.White
        )
    ) {
        Text(
            text = stringResource(R.string.view_location),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}