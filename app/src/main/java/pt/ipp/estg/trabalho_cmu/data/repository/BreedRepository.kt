package pt.ipp.estg.trabalho_cmu.data.repository


import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.CatBreedsResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.DogBreedsResponse
import pt.ipp.estg.trabalho_cmu.providers.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BreedRepository {

    private val dogApi = RetrofitInstance.dogApi
    private val catApi = RetrofitInstance.catApi

    private val translationRepo = TranslationRepository()


    fun getDogBreeds(
        onSuccess: (List<Breed>) -> Unit,
        onError: (String) -> Unit
    ) {
        dogApi.getAllBreeds().enqueue(object : Callback<List<DogBreedsResponse>> {
            override fun onResponse(
                call: Call<List<DogBreedsResponse>>,
                response: Response<List<DogBreedsResponse>>
            ) {
                if (response.isSuccessful) {
                    val breeds = response.body()?.map { dog ->
                        Breed(
                            id = dog.id.toString(),
                            name = dog.name,
                            description = dog.bredFor ?: dog.temperament ?: dog.origin ?: "Raça de cão"
                        )
                    } ?: emptyList()

                    translateBreeds(breeds, onSuccess, onError)

                } else {
                    onError("Erro a carregar raças: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<DogBreedsResponse>>, t: Throwable) {
                onError("Erro de conexão: ${t.message}")
            }
        })
    }


    fun getCatBreeds(
        onSuccess: (List<Breed>) -> Unit,
        onError: (String) -> Unit
    ) {
        catApi.getAllBreeds().enqueue(object : Callback<List<CatBreedsResponse>> {
            override fun onResponse(
                call: Call<List<CatBreedsResponse>>,
                response: Response<List<CatBreedsResponse>>
            ) {
                if (response.isSuccessful) {
                    val breeds = response.body()?.map { cat ->
                        Breed(
                            id = cat.id,
                            name = cat.name,
                            description = cat.description ?: "Raça de gato"
                        )
                    } ?: emptyList()

                    translateBreeds(breeds, onSuccess, onError)

                } else {
                    onError("Erro ao carregar raças: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<CatBreedsResponse>>, t: Throwable) {
                onError("Erro de conexão: ${t.message}")
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

    private fun translateBreeds(
        breeds: List<Breed>,
        onSuccess: (List<Breed>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (breeds.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        val translatedBreeds = mutableListOf<Breed>()
        var pending = breeds.size

        breeds.forEach { breed ->
            translationRepo.translateToPortuguese(
                text = breed.description ?: "",
                onSuccess = { translated ->
                    translatedBreeds.add(
                        breed.copy(description = translated)
                    )

                    pending--
                    if (pending == 0) {
                        onSuccess(translatedBreeds.sortedBy { it.name })
                    }
                },
                onError = { _ ->
                    // fallback to keep original description
                    translatedBreeds.add(breed)
                    pending--
                    if (pending == 0) {
                        onSuccess(translatedBreeds.sortedBy { it.name })
                    }
                }
            )
        }
    }

}