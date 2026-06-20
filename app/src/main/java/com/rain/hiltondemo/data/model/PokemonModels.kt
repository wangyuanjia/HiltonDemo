package com.rain.hiltondemo.data.model

/**
 * Data models for Pokémon GraphQL API responses.
 * Uses Gson for JSON deserialization from https://beta.pokeapi.co/graphql/v1beta
 */

// Top-level GraphQL response wrapper
data class GraphQLResponse<T>(
    val data: T
)

// Species search result container
data class PokemonSpeciesResponse(
    val pokemon_v2_pokemonspecies: List<PokemonSpecies>,
    val pokemon_v2_pokemonspecies_aggregate: AggregateWrapper? = null
)

data class AggregateWrapper(
    val aggregate: Aggregate
)

data class Aggregate(
    val count: Int
)

data class PokemonSpecies(
    val name: String,
    val capture_rate: Int,
    val pokemon_v2_pokemons: List<Pokemon> = emptyList(),
    val pokemon_v2_pokemoncolor: PokemonColor? = null
)

data class Pokemon(
    val name: String,
    val id: Int,
    val pokemon_v2_pokemonabilities: List<PokemonAbilityWrapper> = emptyList()
)

data class PokemonAbilityWrapper(
    val pokemon_v2_ability: Ability
)

data class Ability(
    val name: String
)

data class PokemonColor(
    val name: String
)
