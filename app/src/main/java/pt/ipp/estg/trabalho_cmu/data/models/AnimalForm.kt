package pt.ipp.estg.trabalho_cmu.data.models


/** * Represents a data model for an animal creation or editing form.
 *
 * This data class is designed to temporarily hold user input from a form UI
 * before it is processed and converted into a full [pt.ipp.estg.trabalho_cmu.data.local.entities.Animal] entity.
 * It contains all the necessary fields for creating a new animal profile.
 *
 * @property name The name of the animal as entered by the user.
 * @property breed The breed of the animal.
 * @property species The species of the animal (e.g., "Dog", "Cat").
 * @property size The size of the animal (e.g., "Small", "Medium").
 * @property birthDate The animal's date of birth, typically in a string format like "YYYY-MM-DD".
 * @property description A text description of the animal.
 * @property imageUrl The resource ID for a placeholder or selected image. Note: The type is `Int`, suggesting it's a drawable resource ID rather than a remote URL string.
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
