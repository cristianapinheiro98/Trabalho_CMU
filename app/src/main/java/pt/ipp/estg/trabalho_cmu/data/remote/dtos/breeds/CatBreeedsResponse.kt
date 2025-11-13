package pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds

import com.google.gson.annotations.SerializedName

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