package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import java.util.Date

@Composable
fun ActivityDateTimeCard(
    pickupDate: String,
    pickupTime: String,
    deliveryDate: String,
    deliveryTime: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${stringResource(R.string.pickup_date)}: $pickupDate",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = "${stringResource(R.string.pickup_time)}: $pickupTime",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = "${stringResource(R.string.delivery_date)}: $deliveryDate",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = "${stringResource(R.string.delivery_time)}: $deliveryTime",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
        }
    }
}