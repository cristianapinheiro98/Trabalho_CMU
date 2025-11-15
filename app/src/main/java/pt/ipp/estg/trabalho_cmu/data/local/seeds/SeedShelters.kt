package pt.ipp.estg.trabalho_cmu.data.local.seeds

import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

object SeedShelters {

    val shelters = listOf(
        Shelter(
            id = 0,
            firebaseUid = null,
            name = "Abrigo Municipal",
            address = "Rua do Abrigo, 123",
            phone = "912345678",
            email = "abrigo@test.com",
            password = "123456"
        ),
        Shelter(
            id = 0,
            firebaseUid = null,
            name = "Patinhas Felizes",
            address = "Avenida dos Animais, 45",
            phone = "934567890",
            email = "patinhas@test.com",
            password = "123456"
        )
    )
}
