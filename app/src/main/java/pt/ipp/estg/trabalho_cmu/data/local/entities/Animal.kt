package pt.ipp.estg.trabalho_cmu.data.local.entities

data class Animal(
    val id: String,
    val nome: String,
    val idade: Int,
    val imagemUrl: String,
    val descricao: String
)
