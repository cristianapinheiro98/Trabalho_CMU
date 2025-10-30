package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun ActivityDatesChosen(
    selectedDates: Set<String>,
    modifier: Modifier = Modifier
) {
    if (selectedDates.isNotEmpty()) {
        val sortedDates = selectedDates.sorted()
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "${stringResource(R.string.visit_start_label)} ${sortedDates.first()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = "${stringResource(R.string.visit_end_label)} ${sortedDates.last()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
        }
    }
}