package pt.ipp.estg.trabalho_cmu.data.repository

import android.util.Log
import pt.ipp.estg.trabalho_cmu.data.remote.api.objects.RetrofitClient
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.models.CatBreedResponse
import pt.ipp.estg.trabalho_cmu.data.models.DogBreedsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BreedRepository {

    private val dogApi = RetrofitClient.dogApiService
    private val catApi = RetrofitClient.catApiService

    /**
     * Obter raças de CÃES
     * Retorna uma lista de Breed com id, nome e descrição
     */
    fun getDogBreeds(
        onSuccess: (List<Breed>) -> Unit,
        onError: (String) -> Unit
    ) {
        dogApi.getAllBreeds().enqueue(object : Callback<DogBreedsResponse> {
            override fun onResponse(
                call: Call<DogBreedsResponse>,
                response: Response<DogBreedsResponse>
            ) {
                if (response.isSuccessful) {
                    val breedsMap = response.body()?.breeds ?: emptyMap()

                    // Converter para lista de Breed
                    val breeds = breedsMap.keys.mapIndexed { index, breedName ->
                        Breed(
                            id = "dog_$index",
                            name = breedName.replaceFirstChar { it.uppercase() },
                            description = "Raça de cão"
                        )
                    }.sortedBy { it.name }

                    onSuccess(breeds)
                    Log.d("BreedRepository", "Dog breeds loaded: ${breeds.size}")
                } else {
                    onError("Erro ao carregar raças de cães: ${response.code()}")
                    Log.e("BreedRepository", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<DogBreedsResponse>, t: Throwable) {
                onError("Erro de conexão: ${t.message}")
                Log.e("BreedRepository", "Failure: ${t.message}", t)
            }
        })
    }

    /**
     * Obter raças de GATOS
     * Retorna uma lista de Breed com id, nome e descrição
     */
    fun getCatBreeds(
        onSuccess: (List<Breed>) -> Unit,
        onError: (String) -> Unit
    ) {
        catApi.getAllBreeds().enqueue(object : Callback<List<CatBreedResponse>> {
            override fun onResponse(
                call: Call<List<CatBreedResponse>>,
                response: Response<List<CatBreedResponse>>
            ) {
                if (response.isSuccessful) {
                    val catBreeds = response.body() ?: emptyList()

                    // Converter para lista de Breed simplificada
                    val breeds = catBreeds.map { catBreed ->
                        Breed(
                            id = catBreed.id,
                            name = catBreed.name,
                            description = catBreed.description ?: "Raça de gato"
                        )
                    }.sortedBy { it.name }

                    onSuccess(breeds)
                    Log.d("BreedRepository", "Cat breeds loaded: ${breeds.size}")
                } else {
                    onError("Erro ao carregar raças de gatos: ${response.code()}")
                    Log.e("BreedRepository", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<CatBreedResponse>>, t: Throwable) {
                onError("Erro de conexão: ${t.message}")
                Log.e("BreedRepository", "Failure: ${t.message}", t)
            }
        })
    }

    /**
     * Obter raças baseado na espécie
     */
    fun getBreedsBySpecies(
        species: String,
        onSuccess: (List<Breed>) -> Unit,
        onError: (String) -> Unit
    ) {
        when (species.lowercase()) {
            "cão", "cao", "dog" -> getDogBreeds(onSuccess, onError)
            "gato", "cat" -> getCatBreeds(onSuccess, onError)
            else -> onError("Espécie não suportada: $species")
        }
    }
}