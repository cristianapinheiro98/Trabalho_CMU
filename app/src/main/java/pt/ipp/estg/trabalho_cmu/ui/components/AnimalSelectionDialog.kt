package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Dialog allowing the user to choose an approved animal for an activity.
 *
 * The dialog displays:
 * - A title
 * - A scrollable list of approved animals
 * - A cancel button
 *
 * If the user has no available animals, an informative message is shown instead.
 */
@Composable
fun AnimalSelectionDialog(
    animals: List<Animal>,
    onAnimalSelected: (Animal) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.choose_animal_title))
        },
        text = {
            if (animals.isEmpty()) {
                Text(text = stringResource(R.string.no_animals_available))
            } else {
                Column {
                    animals.forEach { animal ->
                        Text(
                            text = animal.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                                .clickable { onAnimalSelected(animal) },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}
