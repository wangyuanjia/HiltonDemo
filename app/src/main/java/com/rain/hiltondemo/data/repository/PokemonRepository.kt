package com.rain.hiltondemo.data.repository

import com.rain.hiltondemo.data.model.PokemonSpecies
import com.rain.hiltondemo.data.model.PokemonSpeciesResponse
import com.rain.hiltondemo.data.network.PokemonApi

/**
 * Repository layer for Pokémon data operations.
 * Abstracts the data source (GraphQL API) from the rest of the app.
 */
class PokemonRepository {

    private val api = PokemonApi()

    /**
     * Search Pokémon species by name with fuzzy matching and pagination.
     */
    suspend fun searchSpecies(name: String, limit: Int = 10, offset: Int = 0): Result<List<PokemonSpecies>> {
        return api.searchSpecies(name, limit, offset)
    }

    /**
     * Get a single Pokémon species by ID.
     */
    suspend fun getPokemonDetail(pokemonId: Int): Result<PokemonSpeciesResponse> {
        return api.getPokemonDetail(pokemonId)
    }
}
