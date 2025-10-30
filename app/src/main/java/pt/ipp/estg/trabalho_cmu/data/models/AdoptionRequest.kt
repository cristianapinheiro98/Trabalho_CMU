package pt.ipp.estg.trabalho_cmu.data.models

data class PedidoAdocao(
    val id: String = "",
    val nome: String = "",
    val email: String = "",
    val animal: String = "",
    val status: String = "PENDING" // ou usa OwnershipStatus se quiseres
)
