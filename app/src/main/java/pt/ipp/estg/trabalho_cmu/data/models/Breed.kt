package pt.ipp.estg.trabalho_cmu.data.models

import com.google.gson.annotations.SerializedName


data class Breed(
    val id: String,
    val name: String,
    val description: String? = null
)