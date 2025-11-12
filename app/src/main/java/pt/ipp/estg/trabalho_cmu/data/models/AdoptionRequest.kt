package pt.ipp.estg.trabalho_cmu.data.models

data class AdoptionRequest(
    val id: String = "",
    val nome: String = "",
    val email: String = "",
    val animal: String = "",
    val status: String = "PENDING"
)
