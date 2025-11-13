package pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds

import com.google.gson.annotations.SerializedName


data class DogBreedResponse(
    val id: Int,
    val name: String,
    val temperament: String?,
    val origin: String?,
    val description: String?, 
    @SerializedName("bred_for") val bredFor: String?,
    @SerializedName("life_span") val lifeSpan: String?
)



