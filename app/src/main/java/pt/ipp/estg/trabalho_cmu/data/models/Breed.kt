package pt.ipp.estg.trabalho_cmu.data.models

import com.google.gson.annotations.SerializedName


data class Breed(
    val id: String,
    val name: String,
    val description: String? = null
)


data class DogBreedsResponse(
    @SerializedName("message")
    val breeds: Map<String, List<String>>,
    @SerializedName("status")
    val status: String
)


data class CatBreedResponse(
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