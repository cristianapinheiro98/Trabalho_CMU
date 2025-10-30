package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun TimeInputFields(
    pickupTime: String,
    deliveryTime: String,
    onPickupTimeChange: (String) -> Unit,
    onDeliveryTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = pickupTime,
            onValueChange = { newText ->
                onPickupTimeChange(formatTimeInput(newText))
            },
            label = { Text(stringResource(R.string.visit_pickup_time)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.width(120.dp)
        )

        OutlinedTextField(
            value = deliveryTime,
            onValueChange = { newText ->
                onDeliveryTimeChange(formatTimeInput(newText))
            },
            label = { Text(stringResource(R.string.visit_delivery_time)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.width(120.dp)
        )
    }
}

// Função utilitária
fun formatTimeInput(newText: String): String {
    if (newText.isEmpty()) return newText

    var formattedText = newText.replace(Regex("[^0-9:]"), "")
    if (formattedText.length in 3..4 && formattedText[2] != ':') {
        formattedText = formattedText.substring(0, 2) + ":" + formattedText.substring(2)
    }
    return if (formattedText.length <= 5) formattedText else formattedText.substring(0, 5)
}