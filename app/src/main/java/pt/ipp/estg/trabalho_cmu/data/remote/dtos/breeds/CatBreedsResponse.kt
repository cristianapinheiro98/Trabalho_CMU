package pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds

import com.google.gson.annotations.SerializedName

/**
 * Represents a cat breed object returned by the remote API.
 *
 * @property id Unique breed identifier.
 * @property name Breed name.
 * @property description Optional description of the breed.
 * @property temperament Behavioral traits.
 * @property origin Geographic origin.
 */
data class CatBreedsResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("temperament")
    val temperament: String?,
    @SerializedName("origin")
    val origin: String?
)
