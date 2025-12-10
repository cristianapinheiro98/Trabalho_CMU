package pt.ipp.estg.trabalho_cmu.data.models

/**
 * Form model used when creating or editing an animal.
 *
 * This structure is typically used by UI screens to collect user input
 * before sending it to Firestore or Room.
 *
 * @property name Animal name.
 * @property breed Animal breed.
 * @property species Species category.
 * @property size Size category (small/medium/large).
 * @property birthDate Birth date formatted as a String.
 * @property description Additional notes about the animal.
 * @property imageUrl Drawable resource ID for local preview.
 */
data class AnimalForm(
    val name: String = "",
    val breed: String = "",
    val species: String = "",
    val size: String = "",
    val birthDate: String = "",
    val description: String = "",
    val imageUrl: Int = 0
)
