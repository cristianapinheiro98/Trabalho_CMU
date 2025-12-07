package pt.ipp.estg.trabalho_cmu.data.repository

import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.CatBreedsResponse
import pt.ipp.estg.trabalho_cmu.data.remote.dtos.breeds.DogBreedsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import pt.ipp.estg.trabalho_cmu.providers.RetrofitInstance

/**
 * Repository responsible for retrieving dog and cat breeds from remote APIs,
 * and optionally translating their descriptions to Portuguese.
 *
 * Main responsibilities:
 * - Fetch dog breeds from Dog API
 * - Fetch cat breeds from Cat API
 * - Convert remote DTOs into unified Breed models
 * - Translate descriptions through TranslationRepository
 * - Provide UI callbacks through onSuccess / onError lambdas
 *
 * All user-visible errors have been converted into string resource
 * identifiers (R.string.*) for localization.
 */
class BreedRepository {

    private val dogApi = RetrofitInstance.dogApi
    private val catApi = RetrofitInstance.catApi
    private val translationRepo = TranslationRepository()

    /**
     * Fetches all dog breeds and forwards results through callbacks.
     */
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
                    onError("R.string.error_loading_breeds")
                }
            }

            override fun onFailure(call: Call<List<DogBreedsResponse>>, t: Throwable) {
                onError("R.string.error_connection")
            }
        })
    }

    /**
     * Fetches all cat breeds and forwards results through callbacks.
     */
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
                    onError("R.string.error_loading_breeds")
                }
            }

            override fun onFailure(call: Call<List<CatBreedsResponse>>, t: Throwable) {
                onError("R.string.error_connection")
            }
        })
    }

    /**
     * Gets breeds based on species (dog/cat).
     */
    fun getBreedsBySpecies(
        species: String,
        onSuccess: (List<Breed>) -> Unit,
        onError: (String) -> Unit
    ) {
        when (species.lowercase()) {
            "cão", "cao", "dog" -> getDogBreeds(onSuccess, onError)
            "gato", "cat" -> getCatBreeds(onSuccess, onError)
            else -> onError("R.string.error_species_not_supported")
        }
    }

    /**
     * Translates breed descriptions to Portuguese using TranslationRepository.
     *
     * If translation fails, the original description is kept.
     */
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
                    translatedBreeds.add(breed.copy(description = translated))
                    pending--
                    if (pending == 0) onSuccess(translatedBreeds.sortedBy { it.name })
                },
                onError = {
                    translatedBreeds.add(breed)
                    pending--
                    if (pending == 0) onSuccess(translatedBreeds.sortedBy { it.name })
                }
            )
        }
    }
}
