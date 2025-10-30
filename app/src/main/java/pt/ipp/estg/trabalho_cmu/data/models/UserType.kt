package pt.ipp.estg.trabalho_cmu.data.models

enum class UserType(val label: String) {
    UTILIZADOR("Utilizador"),
    ABRIGO("Abrigo");

    override fun toString(): String = label
}
