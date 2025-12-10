package pt.ipp.estg.trabalho_cmu.data.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a breed entry retrieved from the remote API.
 *
 * @property id Unique breed identifier.
 * @property name Breed name.
 * @property description Optional additional information.
 */
data class Breed(
    val id: String,
    val name: String,
    val description: String? = null
)
