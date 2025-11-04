package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.models.PedidoAdocao

class AdminRepository(private val db: AppDatabase) {

    suspend fun getAllPedidos(): List<PedidoAdocao> {
        // Simulado — substitui por DAO real se já tiveres
        return listOf(
            PedidoAdocao("1", "José Lemos", "joselemos@example.com", "Bolinhas"),
            PedidoAdocao("2", "Maria Silva", "maria@example.com", "Luna")
        )
    }

    suspend fun deletePedidoById(id: String) {
        // db.pedidoDao().deleteById(id)
    }

    suspend fun addAnimal(animal: Animal) {
        db.animalDao().insertAnimal(animal)
    }
}
