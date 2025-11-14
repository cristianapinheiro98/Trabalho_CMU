package pt.ipp.estg.trabalho_cmu.data.local.seeds

import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

object SeedAnimals {

    private const val PACKAGE = "pt.ipp.estg.trabalho_cmu"

    private fun drawable(name: String) =
        "android.resource://$PACKAGE/drawable/$name"

    val animals = listOf(
        Animal(
            id = 0,
            name = "Mimi",
            breed = "Europeu Comum",
            species = "Gato",
            size = "Pequeno",
            birthDate = "2020-01-01",
            description = "Muito meiga e adora colo",
            imageUrls = listOf(
                drawable("gato1"),
                drawable("gato2")
            ),
            shelterId = 1,
            status = AnimalStatus.AVAILABLE
        ),
        Animal(
            id = 0,
            name = "Luna",
            breed = "Siames",
            species = "Gato",
            size = "Pequeno",
            birthDate = "2019-06-12",
            description = "Calma, brincalhona e adora crianças",
            imageUrls = listOf(
                drawable("gato3"),
                drawable("gato4")
            ),
            shelterId = 1,
            status = AnimalStatus.AVAILABLE
        ),
        Animal(
            id = 0,
            name = "Nina",
            breed = "Europeu Comum",
            species = "Gato",
            size = "Pequeno",
            birthDate = "2021-03-15",
            description = "Muito curiosa e sociável",
            imageUrls = listOf(
                drawable("gato5"),
                drawable("gato6")
            ),
            shelterId = 1,
            status = AnimalStatus.AVAILABLE
        )
    )
}
