package pt.ipp.estg.trabalho_cmu.data.local.seeds

import pt.ipp.estg.trabalho_cmu.data.local.entities.User

object SeedUsers {

    val users = listOf(
        User(
            id = 1,
            firebaseUid = null,
            name = "Sofia Vaz",
            adress = "Rua das Flores 15, Porto",
            email = "sofia@example.com",
            phone = "912345678",
            password = "password123"
        ),
        User(
            id = 2,
            firebaseUid = null,
            name = "Carlos Almeida",
            adress = "Av. Central 98, Lisboa",
            email = "carlos@example.com",
            phone = "938765432",
            password = "mypassword"
        )
    )
}
