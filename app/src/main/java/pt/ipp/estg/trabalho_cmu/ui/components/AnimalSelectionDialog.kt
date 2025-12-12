package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Dialog allowing the user to choose an approved animal for an activity.
 *
 * The dialog displays:
 * - A title
 * - A scrollable list of approved animals with images
 * - A loading indicator when data is being fetched
 * - A cancel button
 *
 * If the user has no available animals, an informative message is shown instead.
 */
@Composable
fun AnimalSelectionDialog(
    animals: List<Animal>,
    isLoading: Boolean = false,
    onAnimalSelected: (Animal) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.choose_animal_title))
        },
        text = {
            when {
                isLoading -> {
                    // Show loading indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                animals.isEmpty() -> {
                    Text(text = stringResource(R.string.no_animals_available))
                }
                else -> {
                    Column {
                        animals.forEach { animal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onAnimalSelected(animal) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Use the first animal image
                                AsyncImage(
                                    model = animal.imageUrls.firstOrNull() ?: "",
                                    contentDescription = animal.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Use animal name
                                Text(
                                    text = animal.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
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