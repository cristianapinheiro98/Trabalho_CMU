package pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds

import com.google.gson.annotations.SerializedName

/**
 * Represents a dog breed object returned by the remote API.
 *
 * @property id Numeric identifier of the breed.
 * @property name Breed name.
 * @property temperament Breed's typical behaviors.
 * @property origin Country or region of origin.
 * @property description Optional description text.
 * @property bredFor Original breeding purpose.
 * @property lifeSpan Expected life span.
 */
data class DogBreedsResponse(
    val id: Int,
    val name: String,
    val temperament: String?,
    val origin: String?,
    val description: String?,
    @SerializedName("bred_for") val bredFor: String?,
    @SerializedName("life_span") val lifeSpan: String?
)
