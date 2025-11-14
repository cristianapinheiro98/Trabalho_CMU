package pt.ipp.estg.trabalho_cmu.data.remote.dtos

import com.google.gson.annotations.SerializedName

data class TranslationRequest(
    val q: String,
    val source: String = "en",
    val target: String = "pt",
    val format: String = "text"
)

data class TranslationResponse(
    @SerializedName("translatedText")
    val translatedText: String
)
