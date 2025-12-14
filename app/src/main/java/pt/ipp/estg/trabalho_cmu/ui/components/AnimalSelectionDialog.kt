package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal

/**
 * Reusable dialog for selecting an animal from a list
 *
 * The dialog displays:
 * - A customizable title
 * - A scrollable list of animals with images and details
 * - A loading indicator when data is being fetched
 * - A cancel button
 *
 * If the user has no available animals, an informative message is shown instead.
 *
 * @param animals List of animals to display for selection
 * @param title Custom title for the dialog (defaults to generic selection title)
 * @param isLoading Whether the animal list is still loading
 * @param showDetails Whether to show species and breed info (default true)
 * @param onAnimalSelected Callback when an animal is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun AnimalSelectionDialog(
    animals: List<Animal>,
    title: String = stringResource(R.string.choose_animal_title),
    isLoading: Boolean = false,
    showDetails: Boolean = true,
    onAnimalSelected: (Animal) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            when {
                isLoading -> {
                    LoadingContent()
                }
                animals.isEmpty() -> {
                    EmptyContent()
                }
                else -> {
                    AnimalListContent(
                        animals = animals,
                        showDetails = showDetails,
                        onAnimalSelected = onAnimalSelected
                    )
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

/**
 * Loading state content for the dialog
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Empty state content when no animals are available
 */
@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_animals_available),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * List content displaying selectable animals
 */
@Composable
private fun AnimalListContent(
    animals: List<Animal>,
    showDetails: Boolean,
    onAnimalSelected: (Animal) -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(animals, key = { it.id }) { animal ->
            AnimalListItem(
                animal = animal,
                showDetails = showDetails,
                onClick = { onAnimalSelected(animal) }
            )
        }
    }
}

/**
 * Single animal item in the selection list
 *
 * @param animal Animal to display
 * @param showDetails Whether to show species and breed info
 * @param onClick Callback when item is clicked
 */
@Composable
private fun AnimalListItem(
    animal: Animal,
    showDetails: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animal image
        AsyncImage(
            model = animal.imageUrls.firstOrNull() ?: "",
            contentDescription = animal.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Animal info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = animal.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            if (showDetails) {
                Text(
                    text = buildDetailString(animal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Build detail string from animal properties
 * Shows species and breed if available
 */
private fun buildDetailString(animal: Animal): String {
    return buildString {
        append(animal.species)
        if (animal.breed.isNotBlank()) {
            append(" - ")
            append(animal.breed)
        }
    }
}