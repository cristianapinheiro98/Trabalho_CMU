package pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds

import com.google.gson.annotations.SerializedName

data class DogBreedsResponse(
    @SerializedName("message")
    val breeds: Map<String, List<String>>,
    @SerializedName("status")
    val status: String
)