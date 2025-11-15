package pt.ipp.estg.trabalho_cmu.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for an animal breed.
 *
 * This data class is likely used to model data retrieved from an external API,
 * holding information about a specific breed.
 *
 * @property id The unique identifier for the breed, often provided by the API.
 * @property name The common name of the breed (e.g., "Labrador Retriever").
 * @property description A nullable string containing a brief description of the breed's
 *           characteristics, history, or temperament. It can be null if no description is available.
 */
data class Breed(
    val id: String,
    val name: String,
    val description: String? = null
)
