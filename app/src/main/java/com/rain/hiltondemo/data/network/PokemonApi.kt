package com.rain.hiltondemo.data.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rain.hiltondemo.data.model.GraphQLResponse
import com.rain.hiltondemo.data.model.PokemonSpecies
import com.rain.hiltondemo.data.model.PokemonSpeciesResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Service class for making GraphQL requests to the Pokémon API.
 */
class PokemonApi {

    private val baseUrl = "https://beta.pokeapi.co/graphql/v1beta"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Search Pokémon species by name (fuzzy match) with pagination.
     * Returns the list of species on success, or throws on failure.
     */
    fun searchSpecies(name: String, limit: Int, offset: Int): Result<List<PokemonSpecies>> {
        return try {
            val query = """
                query SearchSpecies(${'$'}where: pokemon_v2_pokemonspecies_bool_exp, ${'$'}limit: Int, ${'$'}offset: Int) {
                    pokemon_v2_pokemonspecies(where: ${'$'}where, limit: ${'$'}limit, offset: ${'$'}offset, order_by: {id: asc}) {
                        name
                        capture_rate
                        pokemon_v2_pokemons {
                            name
                            id
                            pokemon_v2_pokemonabilities {
                                pokemon_v2_ability {
                                    name
                                }
                            }
                        }
                        pokemon_v2_pokemoncolor {
                            name
                        }
                    }
                }
            """.trimIndent()

            val variables = mapOf(
                "where" to mapOf("name" to mapOf("_like" to "%$name%")),
                "limit" to limit,
                "offset" to offset
            )

            val requestBody = mapOf(
                "query" to query,
                "variables" to variables
            )

            val jsonBody = gson.toJson(requestBody)
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val request = Request.Builder()
                .url(baseUrl)
                .post(jsonBody.toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return Result.failure(Exception("Empty response"))

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }

            // Parse the response
            val type = object : TypeToken<GraphQLResponse<PokemonSpeciesResponse>>() {}.type
            val graphQLResponse: GraphQLResponse<PokemonSpeciesResponse> = gson.fromJson(responseBody, type)

            Result.success(graphQLResponse.data.pokemon_v2_pokemonspecies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch a single Pokémon's details including abilities.
     */
    fun getPokemonDetail(pokemonId: Int): Result<PokemonSpeciesResponse> {
        return try {
            val query = """
                query PokemonDetail(${'$'}id: Int!) {
                    pokemon_v2_pokemonspecies(where: {id: {_eq: ${'$'}id}}) {
                        name
                        capture_rate
                        pokemon_v2_pokemons {
                            name
                            id
                            pokemon_v2_pokemonabilities {
                                pokemon_v2_ability {
                                    name
                                }
                            }
                        }
                        pokemon_v2_pokemoncolor {
                            name
                        }
                    }
                }
            """.trimIndent()

            val variables = mapOf("id" to pokemonId)
            val requestBody = mapOf("query" to query, "variables" to variables)

            val jsonBody = gson.toJson(requestBody)
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val request = Request.Builder()
                .url(baseUrl)
                .post(jsonBody.toRequestBody(mediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return Result.failure(Exception("Empty response"))

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}"))
            }

            val type = object : TypeToken<GraphQLResponse<PokemonSpeciesResponse>>() {}.type
            val graphQLResponse: GraphQLResponse<PokemonSpeciesResponse> = gson.fromJson(responseBody, type)

            Result.success(graphQLResponse.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
